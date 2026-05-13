package net.noti_me.dymit.dymit_backend_api.application.file.impl

import java.nio.charset.StandardCharsets

/**
 * JPEG 및 PNG 이미지의 방향 메타데이터를 읽습니다.
 */
internal object ImageOrientationMetadataReader {

    /**
     * 이미지 포맷에 따라 방향 메타데이터를 추출합니다.
     *
     * @param imageBytes 원본 이미지 바이트 배열
     * @return 방향 메타데이터, 없으면 null
     */
    fun readOrientation(imageBytes: ByteArray): ImageOrientation? {
        return when {
            isJpeg(imageBytes) -> readJpegOrientation(imageBytes)
            isPng(imageBytes) -> readPngOrientation(imageBytes)
            else -> null
        }
    }

    /**
     * JPEG 헤더를 기반으로 EXIF 방향 정보를 추출합니다.
     *
     * @param imageBytes JPEG 이미지 바이트 배열
     * @return 방향 메타데이터, 없으면 null
     */
    private fun readJpegOrientation(imageBytes: ByteArray): ImageOrientation? {
        var offset = JPEG_SOI_SIZE

        while (offset + 1 < imageBytes.size) {
            if ( imageBytes[offset] != MARKER_PREFIX ) {
                return null
            }

            while (offset < imageBytes.size && imageBytes[offset] == MARKER_PREFIX) {
                offset += 1
            }

            if ( offset >= imageBytes.size ) {
                return null
            }

            val marker = imageBytes[offset].toInt() and BYTE_MASK
            if ( marker == START_OF_SCAN || marker == END_OF_IMAGE ) {
                return null
            }

            if ( marker in STANDALONE_MARKERS ) {
                offset += 1
                continue
            }

            if ( offset + JPEG_SEGMENT_LENGTH_SIZE >= imageBytes.size ) {
                return null
            }

            val segmentLength = readUnsignedShort(
                bytes = imageBytes,
                offset = offset + 1,
                byteOrder = ExifByteOrder.BIG_ENDIAN
            )
            if ( segmentLength < MIN_SEGMENT_LENGTH ) {
                return null
            }

            val segmentDataStart = offset + 1 + JPEG_SEGMENT_LENGTH_SIZE
            val segmentEndExclusive = offset + 1 + segmentLength
            if ( segmentEndExclusive > imageBytes.size ) {
                return null
            }

            if ( marker == APP1_MARKER ) {
                val orientation = readExifOrientation(
                    imageBytes = imageBytes,
                    offset = segmentDataStart,
                    endExclusive = segmentEndExclusive
                )
                if ( orientation != null ) {
                    return orientation
                }
            }

            offset = segmentEndExclusive
        }

        return null
    }

    /**
     * PNG 청크를 순회하며 eXIf 방향 정보를 추출합니다.
     *
     * @param imageBytes PNG 이미지 바이트 배열
     * @return 방향 메타데이터, 없으면 null
     */
    private fun readPngOrientation(imageBytes: ByteArray): ImageOrientation? {
        var offset = PNG_SIGNATURE.size

        while (offset + PNG_CHUNK_OVERHEAD <= imageBytes.size) {
            val chunkLength = readUnsignedInt(
                bytes = imageBytes,
                offset = offset,
                byteOrder = ExifByteOrder.BIG_ENDIAN
            ).toInt()
            if ( chunkLength < 0 ) {
                return null
            }

            val chunkTypeOffset = offset + PNG_LENGTH_FIELD_SIZE
            val chunkDataOffset = chunkTypeOffset + PNG_TYPE_FIELD_SIZE
            val chunkEndExclusive = chunkDataOffset + chunkLength + PNG_CRC_FIELD_SIZE
            if ( chunkEndExclusive > imageBytes.size ) {
                return null
            }

            val chunkType = String(
                imageBytes,
                chunkTypeOffset,
                PNG_TYPE_FIELD_SIZE,
                StandardCharsets.US_ASCII
            )
            if ( chunkType == PNG_EXIF_CHUNK_TYPE ) {
                return readExifOrientation(
                    imageBytes = imageBytes,
                    offset = chunkDataOffset,
                    endExclusive = chunkDataOffset + chunkLength
                )
            }

            offset = chunkEndExclusive
        }

        return null
    }

    /**
     * EXIF 블록에서 방향 태그를 추출합니다.
     *
     * @param imageBytes 원본 이미지 바이트 배열
     * @param offset EXIF 블록 시작 위치
     * @param endExclusive EXIF 블록 종료 위치
     * @return 방향 메타데이터, 없으면 null
     */
    private fun readExifOrientation(
        imageBytes: ByteArray,
        offset: Int,
        endExclusive: Int
    ): ImageOrientation? {
        var tiffOffset = offset
        if ( endExclusive - offset >= EXIF_HEADER.size &&
            imageBytes.copyOfRange(offset, offset + EXIF_HEADER.size).contentEquals(EXIF_HEADER)
        ) {
            tiffOffset += EXIF_HEADER.size
        }

        if ( tiffOffset + TIFF_HEADER_SIZE > endExclusive ) {
            return null
        }

        val byteOrder = when {
            imageBytes[tiffOffset] == LITTLE_ENDIAN_PREFIX &&
                imageBytes[tiffOffset + 1] == LITTLE_ENDIAN_PREFIX -> ExifByteOrder.LITTLE_ENDIAN
            imageBytes[tiffOffset] == BIG_ENDIAN_PREFIX &&
                imageBytes[tiffOffset + 1] == BIG_ENDIAN_PREFIX -> ExifByteOrder.BIG_ENDIAN
            else -> return null
        }

        val tiffMarker = readUnsignedShort(imageBytes, tiffOffset + 2, byteOrder)
        if ( tiffMarker != TIFF_MARKER ) {
            return null
        }

        val ifdOffset = readUnsignedInt(imageBytes, tiffOffset + 4, byteOrder).toInt()
        val ifdStart = tiffOffset + ifdOffset
        if ( ifdOffset < 0 || ifdStart < tiffOffset || ifdStart + TIFF_ENTRY_COUNT_SIZE > endExclusive ) {
            return null
        }

        val entryCount = readUnsignedShort(imageBytes, ifdStart, byteOrder)
        var entryOffset = ifdStart + TIFF_ENTRY_COUNT_SIZE

        repeat(entryCount) {
            if ( entryOffset + TIFF_ENTRY_SIZE > endExclusive ) {
                return null
            }

            val tag = readUnsignedShort(imageBytes, entryOffset, byteOrder)
            if ( tag == ORIENTATION_TAG ) {
                return readOrientationValue(imageBytes, entryOffset, byteOrder)
            }

            entryOffset += TIFF_ENTRY_SIZE
        }

        return null
    }

    /**
     * TIFF 엔트리에서 방향 값을 읽어 `ImageOrientation`으로 변환합니다.
     *
     * @param imageBytes 원본 이미지 바이트 배열
     * @param entryOffset TIFF 엔트리 시작 위치
     * @param byteOrder TIFF 바이트 오더
     * @return 방향 메타데이터, 없으면 null
     */
    private fun readOrientationValue(
        imageBytes: ByteArray,
        entryOffset: Int,
        byteOrder: ExifByteOrder
    ): ImageOrientation? {
        val type = readUnsignedShort(imageBytes, entryOffset + 2, byteOrder)
        val componentCount = readUnsignedInt(imageBytes, entryOffset + 4, byteOrder)
        if ( type != TIFF_SHORT_TYPE || componentCount < 1 ) {
            return null
        }

        val orientationValue = readUnsignedShort(imageBytes, entryOffset + 8, byteOrder)
        return ImageOrientation.fromValue(orientationValue)
    }

    /**
     * 16비트 부호 없는 정수를 읽습니다.
     *
     * @param bytes 원본 바이트 배열
     * @param offset 읽기 시작 위치
     * @param byteOrder 바이트 오더
     * @return 해석된 16비트 정수
     */
    private fun readUnsignedShort(
        bytes: ByteArray,
        offset: Int,
        byteOrder: ExifByteOrder
    ): Int {
        if ( offset + 2 > bytes.size ) {
            return -1
        }

        val first = bytes[offset].toInt() and BYTE_MASK
        val second = bytes[offset + 1].toInt() and BYTE_MASK
        return if ( byteOrder == ExifByteOrder.LITTLE_ENDIAN ) {
            first or (second shl BYTE_SIZE)
        } else {
            (first shl BYTE_SIZE) or second
        }
    }

    /**
     * 32비트 부호 없는 정수를 읽습니다.
     *
     * @param bytes 원본 바이트 배열
     * @param offset 읽기 시작 위치
     * @param byteOrder 바이트 오더
     * @return 해석된 32비트 정수
     */
    private fun readUnsignedInt(
        bytes: ByteArray,
        offset: Int,
        byteOrder: ExifByteOrder
    ): Long {
        if ( offset + 4 > bytes.size ) {
            return -1
        }

        val first = bytes[offset].toLong() and BYTE_MASK_LONG
        val second = bytes[offset + 1].toLong() and BYTE_MASK_LONG
        val third = bytes[offset + 2].toLong() and BYTE_MASK_LONG
        val fourth = bytes[offset + 3].toLong() and BYTE_MASK_LONG

        return if ( byteOrder == ExifByteOrder.LITTLE_ENDIAN ) {
            first or
                (second shl BYTE_SIZE) or
                (third shl (BYTE_SIZE * 2)) or
                (fourth shl (BYTE_SIZE * 3))
        } else {
            (first shl (BYTE_SIZE * 3)) or
                (second shl (BYTE_SIZE * 2)) or
                (third shl BYTE_SIZE) or
                fourth
        }
    }

    /**
     * JPEG 시그니처 여부를 확인합니다.
     *
     * @param imageBytes 원본 이미지 바이트 배열
     * @return JPEG 여부
     */
    private fun isJpeg(imageBytes: ByteArray): Boolean {
        return imageBytes.size >= JPEG_SIGNATURE.size &&
            imageBytes.copyOfRange(0, JPEG_SIGNATURE.size).contentEquals(JPEG_SIGNATURE)
    }

    /**
     * PNG 시그니처 여부를 확인합니다.
     *
     * @param imageBytes 원본 이미지 바이트 배열
     * @return PNG 여부
     */
    private fun isPng(imageBytes: ByteArray): Boolean {
        return imageBytes.size >= PNG_SIGNATURE.size &&
            imageBytes.copyOfRange(0, PNG_SIGNATURE.size).contentEquals(PNG_SIGNATURE)
    }

    /**
     * EXIF 바이트 오더를 표현합니다.
     */
    private enum class ExifByteOrder {
        LITTLE_ENDIAN,
        BIG_ENDIAN
    }

    private val JPEG_SIGNATURE = byteArrayOf(
        0xFF.toByte(),
        0xD8.toByte(),
        0xFF.toByte()
    )

    private val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(),
        0x50.toByte(),
        0x4E.toByte(),
        0x47.toByte(),
        0x0D.toByte(),
        0x0A.toByte(),
        0x1A.toByte(),
        0x0A.toByte()
    )

    private val EXIF_HEADER = byteArrayOf(
        0x45.toByte(),
        0x78.toByte(),
        0x69.toByte(),
        0x66.toByte(),
        0x00.toByte(),
        0x00.toByte()
    )

    private val STANDALONE_MARKERS = setOf(0x01) + (0xD0..0xD7)
    private val MARKER_PREFIX = 0xFF.toByte()

    private const val APP1_MARKER = 0xE1
    private const val START_OF_SCAN = 0xDA
    private const val END_OF_IMAGE = 0xD9
    private const val TIFF_MARKER = 42
    private const val TIFF_SHORT_TYPE = 3
    private const val ORIENTATION_TAG = 0x0112
    private const val PNG_EXIF_CHUNK_TYPE = "eXIf"
    private const val JPEG_SOI_SIZE = 2
    private const val JPEG_SEGMENT_LENGTH_SIZE = 2
    private const val MIN_SEGMENT_LENGTH = 2
    private const val TIFF_HEADER_SIZE = 8
    private const val TIFF_ENTRY_COUNT_SIZE = 2
    private const val TIFF_ENTRY_SIZE = 12
    private const val PNG_LENGTH_FIELD_SIZE = 4
    private const val PNG_TYPE_FIELD_SIZE = 4
    private const val PNG_CRC_FIELD_SIZE = 4
    private const val PNG_CHUNK_OVERHEAD = 12
    private const val BYTE_MASK = 0xFF
    private const val BYTE_SIZE = 8
    private const val BYTE_MASK_LONG = 0xFFL
    private const val LITTLE_ENDIAN_PREFIX: Byte = 0x49
    private const val BIG_ENDIAN_PREFIX: Byte = 0x4D
}

/**
 * EXIF 방향 메타데이터 값을 표현합니다.
 *
 * @param value EXIF orientation 값
 * @param requiresDimensionSwap 변환 시 가로세로 교체 여부
 */
internal enum class ImageOrientation(
    val value: Int,
    val requiresDimensionSwap: Boolean
) {
    NORMAL(value = 1, requiresDimensionSwap = false),
    FLIP_HORIZONTAL(value = 2, requiresDimensionSwap = false),
    ROTATE_180(value = 3, requiresDimensionSwap = false),
    FLIP_VERTICAL(value = 4, requiresDimensionSwap = false),
    TRANSPOSE(value = 5, requiresDimensionSwap = true),
    ROTATE_90(value = 6, requiresDimensionSwap = true),
    TRANSVERSE(value = 7, requiresDimensionSwap = true),
    ROTATE_270(value = 8, requiresDimensionSwap = true);

    companion object {

        /**
         * EXIF 숫자 값을 열거형으로 변환합니다.
         *
         * @param value EXIF orientation 값
         * @return 매칭되는 방향 메타데이터, 없으면 null
         */
        fun fromValue(value: Int): ImageOrientation? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
