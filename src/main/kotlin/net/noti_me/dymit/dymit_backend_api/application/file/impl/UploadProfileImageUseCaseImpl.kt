package net.noti_me.dymit.dymit_backend_api.application.file.impl

import net.noti_me.dymit.dymit_backend_api.application.file.FileIOService
import net.noti_me.dymit.dymit_backend_api.application.file.dto.FileUploadResult
import net.noti_me.dymit.dymit_backend_api.application.file.usecases.UploadProfileImageUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID


@Service
class UploadProfileImageUseCaseImpl(
    private val fileIOService: FileIOService
): UploadProfileImageUseCase {

    companion object {
        private val allowedMimeTypes: Set<String> = setOf(
            "image/jpeg",
            "image/png",
        )
        
        private val magicNumbers: Map<String, String> = mapOf(
            "FFD8FF" to "image/jpeg",
            "89504E47" to "image/png",
        )
    }

    override fun upload(member: MemberInfo, imageFile: MultipartFile): FileUploadResult {
        val randomId = UUID.randomUUID().toString()
        val ext = when (imageFile.contentType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> throw BadRequestException(message="지원하지 않는 이미지 파일 형식입니다.(jpeg, png 만 지원)")
        }
        val bucket = "dymit"
        val path = "/images/${randomId[0]}/${randomId[1]}/${randomId}.${ext}"

        return fileIOService.putFile(
            fileData = imageFile,
            bucket = bucket,
            path = path
        )
    }

    private fun validateMimeType(file: MultipartFile) {
        val allowedMimeTypes = setOf(
            "image/jpeg",
            "image/png",
        )

        if (file.contentType !in allowedMimeTypes) {
            throw IllegalArgumentException("Unsupported file type.")
        }
    }

    private fun validateMagicNumbers(file: MultipartFile) {
        val fileHeader = ByteArray(8)
        file.inputStream.read(fileHeader, 0, 8)
        val fileHeaderHex = fileHeader.joinToString("") { String.format("%02X", it) }

        val isValid = magicNumbers.keys.any { fileHeaderHex.startsWith(it) }
        if (!isValid) {
            throw BadRequestException(message="지원하지 않는 이미지 파일 형식입니다.(jpeg, png 만 지원)")
        }
    }
}

