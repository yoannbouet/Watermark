package watermark

import java.util.MissingFormatArgumentException
import kotlin.system.exitProcess

interface Messaging {
    val msg: String
    fun show(vararg strings: String) {
        try {
            println(this.msg.format(*strings)).also {
                if (this in Errors.values()) exitProcess(0)
            }
        } catch(E: MissingFormatArgumentException) {
            println("$this: Invalid number of arguments.")
            exitProcess(0)
        }
    }
}

enum class Input(override val msg: String): Messaging {
    Image("Input the %s filename:"),
    WatermarkTransparency("Input the watermark transparency percentage (Integer 0-100):"),
    TransparencyColorPrompt("Do you want to set a transparency color?"),
    TransparencyColor("Input a transparency color ([Red] [Green] [Blue]):"),
    Output("Input the output image filename (jpg or png extension):"),
    OutputCreated("The watermarked image %s has been created."),
    AlphaChannel("Do you want to use the watermark's Alpha channel?"),
    PositionMethod("Choose the position method (single, grid):"),
    WatermarkPositions("Input the watermark position ([x 0-%s] [y 0-%s]):")
}

enum class Errors(override val msg: String): Messaging {
    FileNotFound("The file %s doesn't exist."),
    WrongColorComponents("The number of %s color components isn't 3."),
    WrongType("The %s isn't 24 or 32-bit."),
    WatermarkTooLarge("The watermark's dimensions are larger."),
    WeightNotInt("The transparency percentage isn't an integer number."),
    WeightOutOfrange("The transparency percentage is out of range."),
    NotJpgOrPng("The output file extension isn't \"jpg\" or \"png\"."),
    InvalidTColor("The transparency color input is invalid."),
    InvalidPositionMethod("The position method input is invalid."),
    PositionInvalid("The position input is invalid."),
    PositionOutOfRange("The position input is out of range.")
}