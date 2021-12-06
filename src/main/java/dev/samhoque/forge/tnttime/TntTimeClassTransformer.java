package dev.samhoque.forge.tnttime;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.lwjgl.Sys;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class TntTimeClassTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }

        if (transformedName.equals("net.minecraft.client.renderer.entity.RenderTNTPrimed")) {
            ClassReader classReader = new ClassReader(basicClass);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

            MethodNode doRender = classNode.methods.stream()
                    .filter(method -> FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(method.desc).equals("(Lnet/minecraft/entity/item/EntityTNTPrimed;DDDFF)V"))
                    .findFirst().orElse(null);

            if (doRender == null) {
                throw new NullPointerException("[TNT Timer] Could not find doRender method");
            }

            MethodInsnNode popMatrix = (MethodInsnNode) doRender.instructions.get(194);

            InsnList list = new InsnList();

            String mainClass = Main.class.getName().replace('.', '/');

            list.add(new FieldInsnNode(Opcodes.GETSTATIC, mainClass, "main", 'L' + mainClass + ';'));
            list.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this instance (Lnet/minecraft/client/renderer/entity/RenderTNTPrimed;)
            list.add(new VarInsnNode(Opcodes.ALOAD, 1)); //entity Lnet/minecraft/entity/item/EntityTNTPrimed;
            list.add(new VarInsnNode(Opcodes.DLOAD, 2)); //x D
            list.add(new VarInsnNode(Opcodes.DLOAD, 4)); //y D
            list.add(new VarInsnNode(Opcodes.DLOAD, 6)); //z D
            list.add(new VarInsnNode(Opcodes.FLOAD, 9)); //partialTicks F
            list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, mainClass, "doRender",
                    "(Lnet/minecraft/client/renderer/entity/RenderTNTPrimed;Lnet/minecraft/entity/item/EntityTNTPrimed;DDDF)V", false));
            doRender.instructions.insert(popMatrix, list);

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }
}
