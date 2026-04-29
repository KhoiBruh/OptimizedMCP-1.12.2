package net.minecraft.util;

public record TypedActionResult<T>(ActionResult type, T result) {

}
