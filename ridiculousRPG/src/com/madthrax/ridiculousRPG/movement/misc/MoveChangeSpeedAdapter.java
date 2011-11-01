package com.madthrax.ridiculousRPG.movement.misc;

import com.madthrax.ridiculousRPG.animations.TileAnimation;
import com.madthrax.ridiculousRPG.events.EventObject;
import com.madthrax.ridiculousRPG.events.Speed;
import com.madthrax.ridiculousRPG.movement.Movable;
import com.madthrax.ridiculousRPG.movement.MovementHandler;

/**
 * This MovementAdapter allows you to change the move speed and
 * the animation speed.<br>
 * This move cannot be blocked.
 */
public class MoveChangeSpeedAdapter extends MovementHandler {
	private Speed newMoveSpeed, newAnimationSpeed;

	protected MoveChangeSpeedAdapter(Speed newMoveSpeed, Speed newAnimationSpeed) {
		this.newMoveSpeed = newMoveSpeed;
		this.newAnimationSpeed = newAnimationSpeed;
	}
	/**
	 * If the animationSpeed is set to null, it will automatically be computed
	 * from the move-distance. (You can always use null if you don't want to
	 * worry about the characters walk and run - animations)
	 */
	public static MovementHandler $(Speed newMoveSpeed, Speed newAnimationSpeed) {
		return new MoveChangeSpeedAdapter(newMoveSpeed, newAnimationSpeed);
	}
	@Override
	public void tryMove(Movable movable, float deltaTime) {
		if (newMoveSpeed!=null) movable.moveSpeed = newMoveSpeed;
		if (movable instanceof EventObject) {
			TileAnimation anim = ((EventObject) movable).getAnimation();
			if (anim != null) anim.animationSpeed = newAnimationSpeed;
		}
		finished = true;
	}
}
