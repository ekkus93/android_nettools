package dev.nettools.android.data.ssh

import dev.nettools.android.domain.model.TransferError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Unit tests for [ErrorMapper], verifying that SSH/IO exceptions are correctly
 * mapped to [TransferError] sealed class values.
 */
class ErrorMapperTest {

    @Test
    fun `maps ConnectException to HostUnreachable`() {
        val result = ErrorMapper.mapException(ConnectException("Connection refused: connect"))
        assertInstanceOf(TransferError.HostUnreachable::class.java, result)
    }

    @Test
    fun `maps UnknownHostException to HostUnreachable`() {
        val result = ErrorMapper.mapException(UnknownHostException("unknown.host"))
        assertInstanceOf(TransferError.HostUnreachable::class.java, result)
    }

    @Test
    fun `maps SocketTimeoutException to HostUnreachable`() {
        val result = ErrorMapper.mapException(SocketTimeoutException("Read timed out"))
        assertInstanceOf(TransferError.HostUnreachable::class.java, result)
    }

    @Test
    fun `maps exception with Connection refused message to HostUnreachable`() {
        val result = ErrorMapper.mapException(RuntimeException("Connection refused to host"))
        assertInstanceOf(TransferError.HostUnreachable::class.java, result)
    }

    @Test
    fun `maps exception with Auth fail message to AuthFailure`() {
        val result = ErrorMapper.mapException(RuntimeException("Auth fail"))
        assertInstanceOf(TransferError.AuthFailure::class.java, result)
    }

    @Test
    fun `maps IOException with No space left to DiskFull`() {
        val result = ErrorMapper.mapException(IOException("No space left on device"))
        assertInstanceOf(TransferError.DiskFull::class.java, result)
    }

    @Test
    fun `maps IOException with disk full message to DiskFull`() {
        val result = ErrorMapper.mapException(IOException("disk full"))
        assertInstanceOf(TransferError.DiskFull::class.java, result)
    }

    @Test
    fun `maps unknown exception to Unknown with original cause`() {
        val original = RuntimeException("something weird happened")
        val result = ErrorMapper.mapException(original)
        assertInstanceOf(TransferError.Unknown::class.java, result)
        val unknown = result as TransferError.Unknown
        assertEquals(original, unknown.rootCause)
    }

    @Test
    fun `AuthFailure message is preserved`() {
        val result = ErrorMapper.mapException(ConnectException("Connection refused"))
        assertTrue((result as TransferError.HostUnreachable).message.contains("Connection refused"))
    }

    @Test
    fun `DiskFull message is preserved`() {
        val result = ErrorMapper.mapException(IOException("No space left on device"))
        assertTrue((result as TransferError.DiskFull).message.contains("No space left"))
    }
}
