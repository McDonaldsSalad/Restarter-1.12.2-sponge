import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldInsnNode;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.InsnNode;
import jdk.internal.org.objectweb.asm.tree.JumpInsnNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.LdcInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import jdk.internal.org.objectweb.asm.tree.TypeInsnNode;
import jdk.internal.org.objectweb.asm.tree.VarInsnNode;

/** Inject a Java 8-compatible automatic-restart toggle into the original Sponge bytecode. */
public final class PatchAutoRestart implements Opcodes {
    private static final String ENGINE = "net/craftersland/restarter/Engine";
    private static final String RR = "net/craftersland/restarter/RR";
    private static final String CONFIG = "net/craftersland/restarter/ConfigHandler";
    private static final String NODE = "ninja/leaping/configurate/ConfigurationNode";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: java PatchAutoRestart <directory containing extracted plugin classes>");
        }
        Path root = Paths.get(args[0]);
        patchConfig(root.resolve(CONFIG + ".class"));
        patchMainMonitor(root.resolve(ENGINE + "$2.class"));
        patchFinalTimer(root.resolve(ENGINE + "$3.class"));
        System.out.println("Patched ConfigHandler, Engine$2 (monitor) and Engine$3 (countdown)");
    }

    private static ClassNode read(Path file) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(file)).accept(node, 0);
        if (node.version != V1_8) {
            throw new IllegalStateException("Expected Java 8 bytecode in " + file + " but found " + node.version);
        }
        return node;
    }

    private static void write(Path file, ClassNode node) throws Exception {
        // Original classes use Java 8 stack-map frames. Recompute them after branch injection.
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            @Override
            protected String getCommonSuperClass(String left, String right) {
                if (left.equals(right)) return left;
                return "java/lang/Object";
            }
        };
        node.accept(writer);
        Files.write(file, writer.toByteArray());
    }

    private static MethodNode find(ClassNode node, String name, String signature) {
        for (MethodNode m : node.methods) {
            if (m.name.equals(name) && m.desc.equals(signature)) return m;
        }
        throw new IllegalStateException("Missing method " + node.name + "." + name + signature);
    }

    private static MethodInsnNode call(MethodNode method, String owner, String name) {
        MethodInsnNode found = null;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode candidate = (MethodInsnNode) instruction;
            if (candidate.owner.equals(owner) && candidate.name.equals(name)) {
                if (found != null) throw new IllegalStateException("Duplicate method call: " + name);
                found = candidate;
            }
        }
        if (found == null) throw new IllegalStateException("Missing bytecode call to " + owner + "." + name);
        return found;
    }

    private static void addSettingsLookup(InsnList insns) {
        insns.add(new VarInsnNode(ALOAD, 0));
        insns.add(new FieldInsnNode(GETFIELD, ENGINE + "$" + ("2"), "this$0", "L" + ENGINE + ";"));
        insns.add(new MethodInsnNode(INVOKESTATIC, ENGINE, "access$1", "(L" + ENGINE + ";)L" + RR + ";", false));
        insns.add(new MethodInsnNode(INVOKEVIRTUAL, RR, "getConfigHandler", "()L" + CONFIG + ";", false));
        insns.add(new MethodInsnNode(INVOKEVIRTUAL, CONFIG, "isAutoRestartEnabled", "()Z", false));
    }

    private static void patchConfig(Path file) throws Exception {
        ClassNode node = read(file);
        for (MethodNode m : node.methods) {
            if (m.name.equals("isAutoRestartEnabled")) throw new IllegalStateException("Already patched " + file);
        }
        MethodNode m = new MethodNode(ACC_PUBLIC, "isAutoRestartEnabled", "()Z", null, null);
        InsnList code = m.instructions;
        LabelNode def = new LabelNode();
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new FieldInsnNode(GETFIELD, CONFIG, "config", "L" + NODE + ";"));
        code.add(new JumpInsnNode(IFNULL, def));
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new FieldInsnNode(GETFIELD, CONFIG, "config", "L" + NODE + ";"));
        code.add(new InsnNode(ICONST_1));
        code.add(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
        code.add(new InsnNode(DUP));
        code.add(new InsnNode(ICONST_0));
        code.add(new LdcInsnNode("AutoRestartEnabled"));
        code.add(new InsnNode(AASTORE));
        code.add(new MethodInsnNode(INVOKEINTERFACE, NODE, "getNode", "([Ljava/lang/Object;)L" + NODE + ";", true));
        code.add(new VarInsnNode(ASTORE, 1));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new MethodInsnNode(INVOKEINTERFACE, NODE, "isVirtual", "()Z", true));
        code.add(new JumpInsnNode(IFNE, def));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new MethodInsnNode(INVOKEINTERFACE, NODE, "getBoolean", "()Z", true));
        code.add(new InsnNode(IRETURN));
        code.add(def);
        code.add(new InsnNode(ICONST_1));
        code.add(new InsnNode(IRETURN));
        node.methods.add(m);
        write(file, node);
    }

    private static void patchMainMonitor(Path file) throws Exception {
        ClassNode node = read(file);
        MethodNode run = find(node, "run", "()V");
        MethodInsnNode updated = call(run, "net/craftersland/restarter/ServerMonitor", "updateData");
        MethodInsnNode tail = call(run, ENGINE, "access$7");
        LabelNode skipAutoRestart = new LabelNode();
        // Existing tail updates other-server counts and sends the advert. It must still run.
        AbstractInsnNode tailStart = tail.getPrevious().getPrevious();
        if (tailStart.getOpcode() != ALOAD || !(tail.getPrevious() instanceof FieldInsnNode)) {
            throw new IllegalStateException("Unexpected monitor tail bytecode");
        }
        run.instructions.insertBefore(tailStart, skipAutoRestart);
        InsnList guard = new InsnList();
        addSettingsLookup(guard);
        guard.add(new JumpInsnNode(IFEQ, skipAutoRestart));
        run.instructions.insert(updated, guard);
        write(file, node);
    }

    private static void patchFinalTimer(Path file) throws Exception {
        ClassNode node = read(file);
        MethodNode run = find(node, "run", "()V");
        LabelNode enabled = new LabelNode();
        LabelNode taskAbsent = new LabelNode();
        LabelNode done = new LabelNode();
        InsnList guard = new InsnList();
        guard.add(new VarInsnNode(ALOAD, 0));
        guard.add(new FieldInsnNode(GETFIELD, ENGINE + "$3", "this$0", "L" + ENGINE + ";"));
        guard.add(new MethodInsnNode(INVOKESTATIC, ENGINE, "access$1", "(L" + ENGINE + ";)L" + RR + ";", false));
        guard.add(new MethodInsnNode(INVOKEVIRTUAL, RR, "getConfigHandler", "()L" + CONFIG + ";", false));
        guard.add(new MethodInsnNode(INVOKEVIRTUAL, CONFIG, "isAutoRestartEnabled", "()Z", false));
        guard.add(new JumpInsnNode(IFNE, enabled));
        guard.add(new InsnNode(ICONST_0));
        guard.add(new MethodInsnNode(INVOKESTATIC, ENGINE, "access$10", "(Z)V", false));
        guard.add(new MethodInsnNode(INVOKESTATIC, "org/spongepowered/api/Sponge", "getScheduler", "()Lorg/spongepowered/api/scheduler/Scheduler;", false));
        guard.add(new MethodInsnNode(INVOKESTATIC, ENGINE, "access$12", "()Ljava/util/UUID;", false));
        guard.add(new MethodInsnNode(INVOKEINTERFACE, "org/spongepowered/api/scheduler/Scheduler", "getTaskById", "(Ljava/util/UUID;)Ljava/util/Optional;", true));
        guard.add(new InsnNode(DUP));
        guard.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/Optional", "isPresent", "()Z", false));
        guard.add(new JumpInsnNode(IFEQ, taskAbsent));
        guard.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/Optional", "get", "()Ljava/lang/Object;", false));
        guard.add(new TypeInsnNode(CHECKCAST, "org/spongepowered/api/scheduler/Task"));
        guard.add(new MethodInsnNode(INVOKEINTERFACE, "org/spongepowered/api/scheduler/Task", "cancel", "()Z", true));
        guard.add(new InsnNode(POP));
        guard.add(new JumpInsnNode(GOTO, done));
        guard.add(taskAbsent);
        guard.add(new InsnNode(POP));
        guard.add(done);
        guard.add(new InsnNode(RETURN));
        guard.add(enabled);
        run.instructions.insert(guard);
        write(file, node);
    }
}