package no.steffenhove.betongkalkulator

fun calculateCylinderVolume(diameter: Double, height: Double): Double {
    val radius = diameter / 2
    return Math.PI * radius * radius * height
}

fun calculateCuboidVolume(length: Double, width: Double, thickness: Double): Double {
    return length * width * thickness
}

fun calculateTriangleVolume(a: Double, b: Double, c: Double, thickness: Double): Double {
    val s = (a + b + c) / 2
    val area = Math.sqrt(s * (s - a) * (s - b) * (s - c))
    return area * thickness
}