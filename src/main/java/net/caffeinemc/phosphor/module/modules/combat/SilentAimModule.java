package net.caffeinemc.phosphor.module.modules.combat;

import net.caffeinemc.phosphor.api.event.events.AttackEvent;
import net.caffeinemc.phosphor.api.event.events.BlockBreakEvent;
import net.caffeinemc.phosphor.api.event.events.HudRenderEvent;
import net.caffeinemc.phosphor.api.event.events.MouseUpdateEvent;
import net.caffeinemc.phosphor.api.event.orbit.EventHandler;
import net.caffeinemc.phosphor.api.event.orbit.EventPriority;
import net.caffeinemc.phosphor.api.util.PlayerUtils;
import net.caffeinemc.phosphor.api.util.RenderUtils;
import net.caffeinemc.phosphor.api.rotation.RotationUtils;
import net.caffeinemc.phosphor.common.Phosphor;
import net.caffeinemc.phosphor.module.Module;
import net.caffeinemc.phosphor.module.setting.settings.BooleanSetting;
import net.caffeinemc.phosphor.module.setting.settings.NumberSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class SilentAimModule extends Module {
    public final BooleanSetting onlyWeapon = new BooleanSetting("Only Weapon", this, true);
    public final NumberSetting maxDistance = new NumberSetting("Max Distance", this, 4d, 3d, 5d, 0.1d);
    public final BooleanSetting lookAtNearest = new BooleanSetting("Look At Nearest Hitbox's Corner", this, false);
    public final NumberSetting yawSpeed = new NumberSetting("Horizontal Speed", this, 4d, 1d, 10d, 0.1d);
    public final NumberSetting pitchSpeed = new NumberSetting("Verical Speed", this, 2d, 0.5d, 10d, 0.1d);
    public final NumberSetting fov = new NumberSetting("FOV", this, 90d, 1d, 180d, 1d);
    public final BooleanSetting fovCircle = new BooleanSetting("FOV Circle", this, true);

    public SilentAimModule() {
        super("SilentAim", "Automatically aims at players and hit them for you.", Category.COMBAT);
    }

    private LivingEntity targetPlayer;
    private boolean doAttack;
    private boolean canAttack;

    @Override
    public void onEnable() {
        targetPlayer = null;
        doAttack = false;
        canAttack = false;
    }

    @Override
    public void onDisable() {
        Phosphor.rotationManager().setRotation(null);
    }

    @EventHandler
    public void onMouseUpdate(MouseUpdateEvent event) {
        if (mc.currentScreen == null) {
            Item mainHandItem = mc.player.getMainHandStack().getItem();

            if (!(mainHandItem instanceof AxeItem || mainHandItem instanceof SwordItem) && onlyWeapon.isEnabled())
                return;

            targetPlayer = PlayerUtils.findNearestEntity(mc.player, maxDistance.getFValue(), true);

            if (targetPlayer == null)
                return;

            Vec3d targetPlayerPos = targetPlayer.getPos();

            if (lookAtNearest.isEnabled()) {
                double halfHitboxSize = (targetPlayer.getBoundingBox().getXLength() / 2) - 0.01d;

                double offsetX = (mc.player.getX() - targetPlayer.getX()) > 0 ? halfHitboxSize : -halfHitboxSize;
                double offsetZ = (mc.player.getZ() - targetPlayer.getZ()) > 0 ? halfHitboxSize : -halfHitboxSize;

                targetPlayerPos = targetPlayerPos.add(offsetX, 0, offsetZ);
            }

            RotationUtils.Rotation targetRot = RotationUtils.getDirection(mc.player, targetPlayerPos);

            if (RotationUtils.getAngleToRotation(targetRot) > fov.getValue() / 2) {
                canAttack = false;
                return;
            }

            canAttack = true;

            float yawStrength = yawSpeed.getFValue() / 50;
            float pitchStrength = pitchSpeed.getFValue() / 50;

            RotationUtils.Rotation serverRotation = Phosphor.rotationManager().getServerRotation();

            float yaw = MathHelper.lerpAngleDegrees(yawStrength, (float) serverRotation.yaw(), (float) targetRot.yaw());
            float pitch = MathHelper.lerpAngleDegrees(pitchStrength, (float) serverRotation.pitch(), (float) targetRot.pitch());

            Phosphor.rotationManager().setRotation(new RotationUtils.Rotation(yaw, pitch, () -> {
                if (doAttack) PlayerUtils.attackEntity(targetPlayer);
                doAttack = false;
            }));
            mc.player.setHeadYaw(yaw);
        }
    }

    @EventHandler
    private void onHudRender(HudRenderEvent event) {
        if (fovCircle.isEnabled()) {
            RenderUtils.Render2D.renderCircle(event.matrices, new Color(255, 255, 255, 80), mc.getWindow().getScaledWidth() / 2, mc.getWindow().getScaledHeight() / 2, fov.getFValue() * 2, 60);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onAttack(AttackEvent event) {
        if (targetPlayer != null) {
            Item mainHandItem = mc.player.getMainHandStack().getItem();

            if (!(mainHandItem instanceof AxeItem || mainHandItem instanceof SwordItem) && onlyWeapon.isEnabled())
                return;

            if (!targetPlayer.isAlive())
                return;

            if (!canAttack)
                return;

            doAttack = true;
            event.cancel();
        }
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        if (targetPlayer != null) {
            Item mainHandItem = mc.player.getMainHandStack().getItem();

            if (!(mainHandItem instanceof AxeItem || mainHandItem instanceof SwordItem) && onlyWeapon.isEnabled())
                return;

            if (!targetPlayer.isAlive())
                return;

            if (!canAttack)
                return;

            event.cancel();
        }
    }
}
