package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

var outputFileName = ""
var inputFileName = ""
var inputImage: BufferedImage? = null
var messageToHide = ""
val bitsList = mutableListOf<String>()
var password = ""

fun main() {
    var outputImage: BufferedImage

    do {
        println("Task (hide, show, exit):")
        val readInput = readln()
        when (readInput) {
            "hide" -> {
                println("Input image file:")
                inputFileName = readln()
                println("Output image file:")
                outputFileName = readln()
                try {
                    inputImage = ImageIO.read(File(inputFileName))
                } catch (e: Exception) {
                    println("Can't read input file!")
                    continue
                }
                println("Message to hide:")
                messageToHide = readln()
                println("Password:")
                password = readln()
                hideMessage(messageToHide, password)

                val inputImagePixelsSize = inputImage!!.height * inputImage!!.height
                if (bitsList.size > inputImagePixelsSize) {
                    println("The input image is not large enough to hold this message.")
                    continue
                }

                outputImage = createNewImage(inputImage!!, bitsList)
                saveImage(outputImage, File(outputFileName))
                println("Message saved in $outputFileName image.")

            }
            "show" -> {
                println("Input image file:")
                inputFileName = readln()
                try {
                    inputImage = ImageIO.read(File(inputFileName))
                } catch (e: Exception) {
                    println("Can't read input file!")
                    continue
                }
                println("Password:")
                password = readln()

                val showMessage = show(inputImage!!)
                println("Message:")
                println(showMessage)
            }
            "exit" -> println("Bye!")
            else -> println("Wrong task: $readInput")
        }
    } while (readInput != "exit")
}

fun show(inputImage: BufferedImage): String {
    var currentBitsList = ""//mutableListOf<String>()
    var byteArray = byteArrayOf()
    var message = ""
    var bitsCounter = 0

loop@    for (y in 0 until inputImage.height) {
        for (x in 0 until inputImage.width) {
            val color = Color(inputImage.getRGB(x, y))
            val blueColorCode = color.blue
            val bit = blueColorCode.and(1)

            if (bitsCounter == 8) {
                byteArray += currentBitsList.toInt(2).toByte()
                bitsCounter = 1
                currentBitsList = bit.toString()

            } else {
                currentBitsList += bit
                bitsCounter++
            }

            if (byteArray.size >= 3) {
                if (byteArray[byteArray.lastIndex].toInt() == 3 &&
                    byteArray[byteArray.lastIndex-1].toInt() == 0 &&
                    byteArray[byteArray.lastIndex-2].toInt() == 0)
                    break@loop
            }
        }
    }
    val newByteArray: ByteArray = byteArray.copyOfRange(0, byteArray.size-3)

    message = encodeMessage(newByteArray, password)
    return message
}

fun encodeMessage(byteArray: ByteArray, password: String): String {
    var encodeMessage = ""

//    если длина сообщения больше длины пароля, дублируем пароль на всю длину сообщения
    var passwordForAllMessage = ""
    val passwordForAllMessageLength = byteArray.size % password.length
    if (byteArray.size > password.length) {
        passwordForAllMessage = password.repeat(byteArray.size / password.length) + password.substring(0,passwordForAllMessageLength)
    } else passwordForAllMessage = password
    val passwordInBytes = passwordForAllMessage.encodeToByteArray()

    for (byte in byteArray.indices) {
//        var finalByteArray = byteArrayOf()
        val charInBinary = byteArray[byte].toString(2).padStart(8, '0')
        val charInBinaryInPassword = passwordInBytes[byte].toString(2).padStart(8, '0')
        var finalCharInBinary = ""

// далее нужно пройти циклом по каждому charInBinary и заменить на charInBinaryInPassword через XOR
        for (bit in charInBinary.indices) {
            val newBit = charInBinary[bit].toInt() xor charInBinaryInPassword[bit].toInt()
            finalCharInBinary += newBit
        }
        encodeMessage += finalCharInBinary.toInt(2).toChar()
    }
    return encodeMessage
}

fun createNewImage(inputImage: BufferedImage, bitsList: MutableList<String>): BufferedImage {
    val image = BufferedImage(inputImage.width, inputImage.height, BufferedImage.TYPE_INT_RGB)
    var currentPixelNumber = 0
    for (y in 0 until inputImage.height) {
        for (x in 0 until inputImage.width) {
            val color = Color(inputImage.getRGB(x, y))
            if (currentPixelNumber <= bitsList.size-1) {
                val newColor = Color(
                    color.red,
                    color.green,
                    setNewColor(color.blue, bitsList[currentPixelNumber].toInt())
                )
                image.setRGB(x, y, newColor.rgb)
                currentPixelNumber ++
            } else image.setRGB(x,y, color.rgb)
        }
    }
    return image
}

fun setNewColor(pixel: Int, pixelNumber: Int): Int {
    return pixel.and(254).or(pixelNumber) % 256
}

fun saveImage(newImage: BufferedImage, outputFile: File) {
    ImageIO.write(newImage, "png", outputFile)
}

fun hideMessage(messageToHide: String, password: String) {

    val currentBitsList = mutableListOf<String>()
    val byte = messageToHide.encodeToByteArray()

//    если длина сообщения больше длины пароля, дублируем пароль на всю длину сообщения
    var passwordForAllMessage = ""
    val passwordForAllMessageLength = messageToHide.length % password.length
    if (messageToHide.length > password.length) {
        passwordForAllMessage = password.repeat(messageToHide.length / password.length) + password.substring(0,passwordForAllMessageLength)
    } else passwordForAllMessage = password
    val passwordInBytes = passwordForAllMessage.encodeToByteArray()


//    тут проходим по каждому символу сообщения - сначала переводим символ в битовую комбинацию из 8-ми бит
//    также переводим каждый символ строчки пароля в битовую комбинацию
//    далее замещаем биты через XOR -  charInBinary XOR charInBinaryInPassword - и новый полученный бит добавляем в лист
    for (i in byte.indices) {
        val charInBinary = byte[i].toString(2).padStart(8, '0')
        val charInBinaryInPassword = passwordInBytes[i].toString(2).padStart(8, '0')
        var finalCharInBinary = ""

// далее нужно пройти циклом по каждому charInBinary и заменить на charInBinaryInPassword через XOR
        for (bit in charInBinary.indices) {
            val newBit = charInBinary[bit].toInt() xor charInBinaryInPassword[bit].toInt()
            finalCharInBinary += newBit
        }
        currentBitsList.add(finalCharInBinary)
    }
//    далее добавляю в конце currentBitsList значение символов-стопов 0, 0, 3 в битовом выражении
    currentBitsList.add("00000000")
    currentBitsList.add("00000000")
    currentBitsList.add("00000011")


// далее создаю БитЛист в который помещаю каждый бит в отдельный элемент массива
    for (i in 0 until currentBitsList.size) {
        for (j in currentBitsList[i]) {
            bitsList.add(j.toString())
        }
    }
    println(bitsList.toString())
}



