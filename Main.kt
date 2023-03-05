package watermark

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class Watermark {
    private var isTranslucent = false
    private lateinit var watermarkTColor: Color
    private var watermarkPosition = Pair<Int?, Int?>(0, 0)

    init {
        /* IMAGE */
        Input.Image.show(Const.IMAGE)
        val imageFileName = readln().also { if (!File(it).exists()) Errors.FileNotFound.show(it) }
        val image = ImageIO.read(File(imageFileName))
        if (image.colorModel.numColorComponents != 3) {
            Errors.WrongColorComponents.show(Const.IMAGE)
        } else if (image.colorModel.pixelSize != 24 && image.colorModel.pixelSize != 32) {
            Errors.WrongType.show(Const.IMAGE)
        }

        /* WATERMARK IMAGE */
        Input.Image.show(Const.WATERMARK + Const.BLANK + Const.IMAGE)
        val watermarkFileName = readln().also { if (!File(it).exists()) Errors.FileNotFound.show(it) }
        val watermark = ImageIO.read(File(watermarkFileName))
        if (watermark.colorModel.numColorComponents != 3) {
            Errors.WrongColorComponents.show(Const.WATERMARK)
        } else if (watermark.colorModel.pixelSize != 24 && watermark.colorModel.pixelSize != 32) {
            Errors.WrongType.show(Const.WATERMARK)
        }
        val watermarkTransparency = when (watermark.transparency) {
            1 -> Const.OPAQUE
            2 -> Const.BITMASK
            else -> Const.TRANSLUCENT
        }

        /* DIMENSIONS */
        if (image.width < watermark.width || image.height < watermark.height) {
            Errors.WatermarkTooLarge.show()
        }

        /* TRANSPARENCY */
        if (watermarkTransparency == Const.TRANSLUCENT) {
            Input.AlphaChannel.show()
            if (readln().lowercase() == Const.YES) isTranslucent = true
        } else if (!isTranslucent) {
            Input.TransparencyColorPrompt.show()
            if (readln().lowercase() == Const.YES) {
                Input.TransparencyColor.show()
                readln().split(Const.BLANK).let { list ->
                    list.forEachIndexed { index, str ->
                        if (str.toIntOrNull() == null || str.toInt() !in 0..255 || index > 2) {
                            Errors.InvalidTColor.show()
                        }
                    }
                    watermarkTColor = Color(list[0].toInt(), list[1].toInt(), list[2].toInt())
                }
            }
        }
        Input.WatermarkTransparency.show()
        val transparencyWeight = readln().toIntOrNull().also {
            if (it == null) {
                Errors.WeightNotInt.show()
            } else if (it !in 0..100) {
                Errors.WeightOutOfrange.show()
            }
        }

        /* OUTPUT */
        Input.PositionMethod.show()
        val bufferedImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        val positionMethod = readln().also { if (it != Const.GRID && it != Const.SINGLE) {
        Errors.InvalidPositionMethod.show() } }
        /* SINGLE */
        if (positionMethod == Const.SINGLE) {
            val widthDifferenceIW = image.width - watermark.width
            val heightDifferenceIW = image.height - watermark.height
            Input.WatermarkPositions.show((widthDifferenceIW).toString(), (heightDifferenceIW).toString())
            readln().split(Const.BLANK).let { coordinates ->
                val first = coordinates.first().toIntOrNull()
                val second = coordinates.last().toIntOrNull()
                if (first == null || second == null) {
                    Errors.PositionInvalid.show()
                } else if (first !in 0..widthDifferenceIW || second !in 0..heightDifferenceIW) {
                    Errors.PositionOutOfRange.show()
                }
                watermarkPosition = Pair(first, second)
            }
            for (y in 0 until image.height) {
                for (x in 0 until image.width) {
                    val watermarkBool = y in watermarkPosition.second!! until watermarkPosition.second!! + watermark.height &&
                            x in watermarkPosition.first!! until watermarkPosition.first!! + watermark.width
                    val iColor = Color(image.getRGB(x, y))

                    if (!watermarkBool) {
                        bufferedImage.setRGB(x, y, iColor.rgb)
                    } else {
                        val wColor = Color(watermark.getRGB(
                            (x + watermarkPosition.first!!) % watermarkPosition.first!!,
                            (y + watermarkPosition.second!!) % watermarkPosition.second!!))
                        val color = Color(
                            (transparencyWeight!! * wColor.red + (100 - transparencyWeight) * iColor.red) / 100,
                            (transparencyWeight * wColor.green + (100 - transparencyWeight) * iColor.green) / 100,
                            (transparencyWeight * wColor.blue + (100 - transparencyWeight) * iColor.blue) / 100
                        ).rgb
                        if (isTranslucent) {
                            if (Color(watermark.getRGB(x - watermarkPosition.first!!, y - watermarkPosition.second!!),true).alpha == 255) {
                                bufferedImage.setRGB(x, y, color)
                            } else {
                                bufferedImage.setRGB(x, y, iColor.rgb)
                            }
                        } else {
                            if (::watermarkTColor.isInitialized && watermarkTColor == wColor) {
                                bufferedImage.setRGB(x, y, iColor.rgb)
                            } else {
                                bufferedImage.setRGB(x, y, color)
                            }
                        }
                    }
                }
            }
        /* GRID */
        } else if (positionMethod == Const.GRID) {
            for (y in 0 until image.height step watermark.height) {
                for (x in 0 until image.width step watermark.width) {
                    for (i in 0 until watermark.height) {
                        for (j in 0 until watermark.width) {
                            if (x + j >= image.width || y + i >= image.height) break
                            val iColor = Color(image.getRGB(x + j, y + i))
                            val wColor = Color(watermark.getRGB(j, i))
                            val color = Color(
                                (transparencyWeight!! * wColor.red + (100 - transparencyWeight) * iColor.red) / 100,
                                (transparencyWeight * wColor.green + (100 - transparencyWeight) * iColor.green) / 100,
                                (transparencyWeight * wColor.blue + (100 - transparencyWeight) * iColor.blue) / 100
                            ).rgb

                            if (isTranslucent) {
                                if (Color(watermark.getRGB(j, i),true).alpha == 255) {
                                    bufferedImage.setRGB(x + j, y + i, color)
                                } else {
                                    bufferedImage.setRGB(x + j, y + i, iColor.rgb)
                                }
                            } else {
                                if (::watermarkTColor.isInitialized && watermarkTColor == wColor) {
                                    bufferedImage.setRGB(x + j, y + i, iColor.rgb)
                                } else {
                                    bufferedImage.setRGB(x + j, y + i, color)
                                }
                            }
                        }
                    }
                }
            }
        }

        Input.Output.show()
        val outputFileName = readln()
        val outputFile = File(outputFileName)
        val format = outputFileName.split(Const.DOT).last().also {
            if (it != "jpg" && it != "png") Errors.NotJpgOrPng.show()
        }
        ImageIO.write(bufferedImage, format, outputFile)
        Input.OutputCreated.show(outputFileName)
    }
}

fun main() {
    Watermark()
}