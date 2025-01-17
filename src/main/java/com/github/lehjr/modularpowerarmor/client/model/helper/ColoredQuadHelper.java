package com.github.lehjr.modularpowerarmor.client.model.helper;

import com.google.common.base.Objects;
import com.github.lehjr.mpalib.math.Colour;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

/*
 * This is just a helper for creating a map key for Guava cache
 */
public class ColoredQuadHelper {
    private final Colour colour;
    private final Direction facing;

    public ColoredQuadHelper(Colour colour, @Nullable Direction facing) {
        this.colour = colour;
        this.facing = facing;
    }

    public Colour getColour() {
        return colour;
    }

    @Nullable
    public Direction getFacing() {
        return facing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColoredQuadHelper that = (ColoredQuadHelper) o;
        return Objects.equal(getColour(), that.getColour()) &&
                getFacing() == that.getFacing();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getColour(), getFacing());
    }
}