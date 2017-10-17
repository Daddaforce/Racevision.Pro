package seng302.models;


/**
 * Created by cjd137 on 21/07/17.
 * A class for assigning penalties to boats
 */
public class Penalties {

    /**
     * When a boat collides with another boat, both get damaged (collider more than collidee)
     * @param boatCollider The boat in the wrong
     * @param boatCollidee The boat that was hit
     */
    public void boatCollision(Boat boatCollider, Boat boatCollidee) {
        if (System.currentTimeMillis() - boatCollider.getTimeSinceLastCollision() > 2500) {
            boatCollider.addDamage(20);
            boatCollidee.addDamage(5);
            boatCollider.setTimeSinceLastCollision(System.currentTimeMillis());

        }
    }

    /**
     * The accrued boat penalty for obstructing another boat, the penalty is minus two boat lengths
     * @param boatCollider the bad boat
     */
    public void boatPenalty(Boat boatCollider) {
        boatCollider.addPenalty(boatCollider.getCurrentSpeed() / 0.02); //lose two boat lengths of time

    }

    /**
     * add damage to a boat that is out of bounds
     * @param boat the boat out of bounds
     */
    public void boatOutOfBounds(Boat boat){
        if (System.currentTimeMillis() - boat.getTimeSinceLastCollision() > 250) {
            boat.addDamage(1);
            boat.setTimeSinceLastCollision(System.currentTimeMillis());
        }
    }

    /**
     * When a boat collides with a mark
     * @param boatCollider the boat which hit the mark
     */
    public void markCollision(Boat boatCollider) {
        if (System.currentTimeMillis() - boatCollider.getTimeSinceLastCollision() > 2500) {
            boatCollider.addDamage(5);
            boatCollider.setTimeSinceLastCollision(System.currentTimeMillis());
        }
    }

    /**
     * When a boat goes out of bounds, the penalty is minus two boat lengths
     * @param boatCollider the bad boat
     */
    public void boundaryPenalty(Boat boatCollider) {
        boatCollider.addPenalty(boatCollider.getCurrentSpeed() / 0.02);
    }

    /**
     * A penalty for starting a race too early, the penalty is minus two boat lengths
     * @param boatCollider the bad boat
     */
    public void earlyStartPenalty(Boat boatCollider) {
        boatCollider.addPenalty(boatCollider.getCurrentSpeed()/0.02);
    }
}
