package net.noti_me.dymit.dymit_backend_api.application.file.impl

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.errors.InternalServerError
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.roundToInt

/**
 * 이미지 메타데이터의 방향 정보를 반영해 썸네일 바이트를 생성합니다.
 */
internal object ThumbnailImageProcessor {

    /**
     * 원본 이미지 바이트로부터 방향이 보정된 썸네일 바이트를 생성합니다.
     *
     * @param imageBytes 원본 이미지 바이트 배열
     * @param maxWidth 썸네일 최대 너비
     * @param maxHeight 썸네일 최대 높이
     * @param outputFormat 출력 이미지 포맷
     * @return 생성된 썸네일 바이트 배열
     */
    fun createThumbnailBytes(
        imageBytes: ByteArray,
        maxWidth: Int,
        maxHeight: Int,
        outputFormat: String
    ): ByteArray {
        val sourceImage = readSourceImage(imageBytes)
        val orientedImage = normalizeOrientation(imageBytes, sourceImage)
        val resizedImage = resizeImage(orientedImage, maxWidth, maxHeight)
        return writeImage(resizedImage, outputFormat)
    }

    /**
     * 바이트 배열을 `BufferedImage`로 읽어옵니다.
     *
     * @param imageBytes 원본 이미지 바이트 배열
     * @return 디코딩된 이미지
     */
    private fun readSourceImage(imageBytes: ByteArray): BufferedImage {
        return try {
            ImageIO.read(ByteArrayInputStream(imageBytes))
        } catch (exception: Exception) {
            throw BadRequestException(message = "유효하지 않은 이미지입니다.")
        } ?: throw BadRequestException(message = "유효하지 않은 이미지입니다.")
    }

    /**
     * 방향 메타데이터를 읽어 이미지 표시 방향을 보정합니다.
     *
     * @param imageBytes 원본 이미지 바이트 배열
     * @param sourceImage 디코딩된 원본 이미지
     * @return 표시 방향이 보정된 이미지
     */
    private fun normalizeOrientation(
        imageBytes: ByteArray,
        sourceImage: BufferedImage
    ): BufferedImage {
        val orientation = ImageOrientationMetadataReader.readOrientation(imageBytes) ?: return sourceImage
        return when (orientation) {
            ImageOrientation.NORMAL -> sourceImage
            else -> transformImage(sourceImage, orientation)
        }
    }

    /**
     * 최대 크기를 유지하면서 원본 비율에 맞게 이미지를 축소합니다.
     *
     * @param sourceImage 방향 보정이 적용된 이미지
     * @param maxWidth 썸네일 최대 너비
     * @param maxHeight 썸네일 최대 높이
     * @return 축소된 이미지
     */
    private fun resizeImage(
        sourceImage: BufferedImage,
        maxWidth: Int,
        maxHeight: Int
    ): BufferedImage {
        val scale = minOf(
            maxWidth.toDouble() / sourceImage.width.toDouble(),
            maxHeight.toDouble() / sourceImage.height.toDouble(),
            1.0
        )
        val targetWidth = (sourceImage.width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (sourceImage.height * scale).roundToInt().coerceAtLeast(1)
        val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
        val graphics = resizedImage.createGraphics()

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.drawImage(sourceImage, 0, 0, targetWidth, targetHeight, null)
        graphics.dispose()

        return resizedImage
    }

    /**
     * 보정된 이미지를 지정한 포맷의 바이트 배열로 인코딩합니다.
     *
     * @param image 인코딩할 이미지
     * @param outputFormat 출력 이미지 포맷
     * @return 인코딩된 이미지 바이트 배열
     */
    private fun writeImage(image: BufferedImage, outputFormat: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val isWritten = ImageIO.write(image, outputFormat, outputStream)

        if ( !isWritten ) {
            throw InternalServerError(message = "썸네일 생성에 실패했습니다.")
        }

        return outputStream.toByteArray()
    }

    /**
     * 방향 메타데이터에 맞게 이미지를 변환합니다.
     *
     * @param sourceImage 방향 보정 전 이미지
     * @param orientation EXIF 방향 값
     * @return 방향 보정된 이미지
     */
    private fun transformImage(
        sourceImage: BufferedImage,
        orientation: ImageOrientation
    ): BufferedImage {
        val targetWidth = if ( orientation.requiresDimensionSwap ) {
            sourceImage.height
        } else {
            sourceImage.width
        }
        val targetHeight = if ( orientation.requiresDimensionSwap ) {
            sourceImage.width
        } else {
            sourceImage.height
        }
        val transformedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
        val graphics = transformedImage.createGraphics()

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.drawImage(sourceImage, createTransform(sourceImage, orientation), null)
        graphics.dispose()

        return transformedImage
    }

    /**
     * EXIF 방향 값에 대응하는 아핀 변환을 생성합니다.
     *
     * @param sourceImage 방향 보정 전 이미지
     * @param orientation EXIF 방향 값
     * @return 이미지 변환에 사용할 아핀 변환
     */
    private fun createTransform(
        sourceImage: BufferedImage,
        orientation: ImageOrientation
    ): AffineTransform {
        val width = sourceImage.width.toDouble()
        val height = sourceImage.height.toDouble()

        return when (orientation) {
            ImageOrientation.NORMAL -> AffineTransform()
            ImageOrientation.FLIP_HORIZONTAL -> AffineTransform(-1.0, 0.0, 0.0, 1.0, width, 0.0)
            ImageOrientation.ROTATE_180 -> AffineTransform(-1.0, 0.0, 0.0, -1.0, width, height)
            ImageOrientation.FLIP_VERTICAL -> AffineTransform(1.0, 0.0, 0.0, -1.0, 0.0, height)
            ImageOrientation.TRANSPOSE -> AffineTransform(0.0, 1.0, 1.0, 0.0, 0.0, 0.0)
            ImageOrientation.ROTATE_90 -> AffineTransform(0.0, 1.0, -1.0, 0.0, height, 0.0)
            ImageOrientation.TRANSVERSE -> AffineTransform(0.0, -1.0, -1.0, 0.0, height, width)
            ImageOrientation.ROTATE_270 -> AffineTransform(0.0, -1.0, 1.0, 0.0, 0.0, width)
        }
    }
}
