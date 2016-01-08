package com.github.lunatrius.schematica.nbt;

import com.github.lunatrius.schematica.reference.Reference;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import scala.Option;
import scala.collection.Iterable;
import scala.collection.JavaConversions;
import scala.collection.mutable.Map;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ForgeMultipart {
    private static boolean enabled = false;
    private static boolean client = false;

    private static Map<String, Object> instancesTypeMap;
    private static Method methodMaterialID;
    private static Method methodCreatePart;
    private static Method methodLoad;
    private static Method methodOnPartChanged;
    private static Object instanceMultipartGenerator$;
    private static Method methodGenerateCompositeTile;
    private static Method methodLoadParts;
    private static Method methodCreateFromNBT;

    public static void init() {
        enabled = Loader.isModLoaded("ForgeMultipart");
        client = FMLCommonHandler.instance().getSide().isClient();

        if (enabled) {
            try {
                final ClassLoader classLoader = ForgeMultipart.class.getClassLoader();

                final Class<? super Object> classMultiPartRegistry$ = ReflectionHelper.getClass(classLoader, "codechicken.multipart.MultiPartRegistry$");
                final Field fieldMultiPartRegistry$module$ = ReflectionHelper.findField(classMultiPartRegistry$, "MODULE$");
                final Field field$typeMap = ReflectionHelper.findField(classMultiPartRegistry$, "codechicken$multipart$MultiPartRegistry$$typeMap");
                final Object instanceMultiPartRegistry$module$ = fieldMultiPartRegistry$module$.get(classMultiPartRegistry$);
                instancesTypeMap = (Map<String, Object>) field$typeMap.get(instanceMultiPartRegistry$module$);

                final Class<? super Object> classMicroMaterialRegistry = ReflectionHelper.getClass(classLoader, "codechicken.microblock.MicroMaterialRegistry");
                methodMaterialID = ReflectionHelper.findMethod(classMicroMaterialRegistry, null, new String[] {
                        "materialID"
                }, String.class);

                final Class<? super Object> classMicroblockClass = ReflectionHelper.getClass(classLoader, "codechicken.microblock.MicroblockClass");
                methodCreatePart = ReflectionHelper.findMethod(classMicroblockClass, null, new String[] {
                        "create"
                }, boolean.class, int.class);

                final Class<? super Object> classTMultiPart = ReflectionHelper.getClass(classLoader, "codechicken.multipart.TMultiPart");
                methodLoad = ReflectionHelper.findMethod(classTMultiPart, null, new String[] {
                        "load"
                }, NBTTagCompound.class);
                methodOnPartChanged = ReflectionHelper.findMethod(classTMultiPart, null, new String[] {
                        "onPartChanged"
                }, classTMultiPart);

                final Class<? super Object> classMultipartGenerator$ = ReflectionHelper.getClass(classLoader, "codechicken.multipart.MultipartGenerator$");
                final Field fieldMultipartGenerator$module$ = ReflectionHelper.findField(classMultipartGenerator$, "MODULE$");
                instanceMultipartGenerator$ = fieldMultipartGenerator$module$.get(classMultipartGenerator$);
                methodGenerateCompositeTile = ReflectionHelper.findMethod(classMultipartGenerator$, null, new String[] {
                        "generateCompositeTile"
                }, TileEntity.class, Iterable.class, boolean.class);

                final Class<? super Object> classTileMultipart = ReflectionHelper.getClass(classLoader, "codechicken.multipart.TileMultipart");
                methodLoadParts = ReflectionHelper.findMethod(classTileMultipart, null, new String[] {
                        "loadParts"
                }, Iterable.class);
                methodCreateFromNBT = ReflectionHelper.findMethod(classTileMultipart, null, new String[] {
                        "createFromNBT"
                }, NBTTagCompound.class);
            } catch (final Exception e) {
                Reference.logger.error("Something went wrong, disabling FMP integration.", e);
                enabled = false;
            }
        }
    }

    public static TileEntity createFromNBT(final NBTTagCompound tileEntityCompound) {
        return createFromNBT(tileEntityCompound, client);
    }

    public static TileEntity createFromNBT(final NBTTagCompound tileEntityCompound, final boolean client) {
        if (!enabled) {
            return null;
        }

        try {
            if (client) {
                return createFromNBTClient(tileEntityCompound);
            } else {
                return createFromNBTServer(tileEntityCompound);
            }
        } catch (final Exception e) {
            Reference.logger.error("Something went wrong!", e);
        }

        return null;
    }

    // ಠ_ಠ
    private static TileEntity createFromNBTClient(final NBTTagCompound tileEntityCompound) throws ReflectiveOperationException {
        final NBTTagList partList = tileEntityCompound.getTagList("parts", Constants.NBT.TAG_COMPOUND);
        final List<Object> parts = new ArrayList<Object>();
        final boolean client = true;

        for (int i = 0; i < partList.tagCount(); i++) {
            final NBTTagCompound partTag = partList.getCompoundTagAt(i);
            final String partID = partTag.getString("id");
            final Integer materialID = materialID(partTag.getString("material"));

            final Object part = createPart(partID, client, materialID);
            if (part != null) {
                load(part, partTag);
                parts.add(part);
            }
        }

        if (parts.size() != partList.tagCount()) {
            Reference.logger.info("Mismatched part size (got {}, expected {})", parts.size(), partList.tagCount());
            return null;
        }

        if (parts.size() == 0) {
            return null;
        }

        final TileEntity tileEntity = generateCompositeTile(null, parts, client);
        tileEntity.readFromNBT(tileEntityCompound);
        loadParts(tileEntity, parts);

        for (Object part : parts) {
            onPartChanged(part, part);
        }

        return tileEntity;
    }

    private static TileEntity createFromNBTServer(final NBTTagCompound tileEntityCompound) {
        try {
            return (TileEntity) methodCreateFromNBT.invoke(null, tileEntityCompound);
        } catch (final Exception e) {
            Reference.logger.warn("Something went wrong!", e);
        }

        return null;
    }

    private static int materialID(final String material) throws ReflectiveOperationException {
        return (Integer) methodMaterialID.invoke(null, material);
    }

    private static Object createPart(final String partID, final boolean client, final int materialID) throws ReflectiveOperationException {
        final Option<Object> option = instancesTypeMap.get(partID);
        if (option.isEmpty()) {
            Reference.logger.trace("Invalid type (found: {})", option);
            return null;
        }

        return methodCreatePart.invoke(option.get(), client, materialID);
    }

    private static Object load(final Object part, final NBTTagCompound partTag) throws ReflectiveOperationException {
        return methodLoad.invoke(part, partTag);
    }

    private static Object onPartChanged(final Object part, final Object part2) throws ReflectiveOperationException {
        return methodOnPartChanged.invoke(part, part2);
    }

    private static TileEntity generateCompositeTile(final TileEntity tileEntity, final List<Object> parts, final boolean client) throws ReflectiveOperationException {
        return (TileEntity) methodGenerateCompositeTile.invoke(instanceMultipartGenerator$, tileEntity, JavaConversions.collectionAsScalaIterable(parts), client);
    }

    private static void loadParts(final TileEntity tileEntity, final List<Object> parts) throws ReflectiveOperationException {
        methodLoadParts.invoke(tileEntity, JavaConversions.collectionAsScalaIterable(parts));
    }
}
