package com.projectronin.clinical.trial.server.clinicalone.auth

import com.projectronin.interop.common.auth.Authentication
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class ClinicalOneAuthenticationBrokerTest {
    private lateinit var authenticationService: ClinicalOneAuthenticationService
    private lateinit var broker: ClinicalOneAuthenticationBroker

    private val cachedAuthorizationProperty =
        ClinicalOneAuthenticationBroker::class.memberProperties
            .first { it.name == "cachedAuthentication" } as KMutableProperty<Authentication>

    init {
        cachedAuthorizationProperty.isAccessible = true
    }

    @BeforeEach
    fun initTest() {
        authenticationService = mockk()
        broker = ClinicalOneAuthenticationBroker(authenticationService)
    }

    @Test
    fun `loads partially specified authentication when not cached`() {
        every { authenticationService.getAuthentication() } returns ClinicalOneAuthentication("Bearer", "token")

        val authentication = broker.getAuthentication()
        Assertions.assertEquals("Bearer", authentication.tokenType)
        Assertions.assertEquals("token", authentication.accessToken)
        Assertions.assertNull(authentication.expiresAt)
        Assertions.assertNull(authentication.scope)
        Assertions.assertNull(authentication.refreshToken)
    }

    @Test
    fun `loads fully specified authentication when not cached`() {
        every { authenticationService.getAuthentication() } returns
            ClinicalOneAuthentication(
                "Bearer",
                "token",
                360,
                "refresh",
                "scope",
            )

        val authentication = broker.getAuthentication()
        Assertions.assertEquals("Bearer", authentication.tokenType)
        Assertions.assertEquals("token", authentication.accessToken)
        Assertions.assertNotNull(authentication.expiresAt)
        Assertions.assertEquals("scope", authentication.scope)
        Assertions.assertEquals("refresh", authentication.refreshToken)
    }

    @Test
    fun `caches authentication when expiration provided`() {
        every { authenticationService.getAuthentication() } returns ClinicalOneAuthentication("Bearer", "token", 360)

        val authentication = broker.getAuthentication()
        Assertions.assertEquals("Bearer", authentication.tokenType)
        Assertions.assertEquals("token", authentication.accessToken)
        Assertions.assertNotNull(authentication.expiresAt)
        Assertions.assertNull(authentication.scope)
        Assertions.assertNull(authentication.refreshToken)

        Assertions.assertEquals(authentication, getCachedAuthentication())
    }

    @Test
    fun `does not cache authentication when no expiration provided`() {
        every { authenticationService.getAuthentication() } returns ClinicalOneAuthentication("Bearer", "token")

        val authentication = broker.getAuthentication()
        Assertions.assertEquals("Bearer", authentication.tokenType)
        Assertions.assertEquals("token", authentication.accessToken)
        Assertions.assertNull(authentication.expiresAt)
        Assertions.assertNull(authentication.scope)
        Assertions.assertNull(authentication.refreshToken)

        Assertions.assertNull(getCachedAuthentication())
    }

    @Test
    fun `loads authentication when cached has no expiration`() {
        val cachedAuthentication =
            mockk<Authentication> {
                every { expiresAt } returns null
            }
        setCachedAuthentication(cachedAuthentication)

        every { authenticationService.getAuthentication() } returns ClinicalOneAuthentication("Bearer", "token")

        val authentication = broker.getAuthentication()
        Assertions.assertEquals("Bearer", authentication.tokenType)
        Assertions.assertEquals("token", authentication.accessToken)
        Assertions.assertNull(authentication.expiresAt)
        Assertions.assertNull(authentication.scope)
        Assertions.assertNull(authentication.refreshToken)
    }

    @Test
    fun `loads authentication when cached has expired`() {
        val cachedAuthentication =
            mockk<Authentication> {
                every { expiresAt } returns Instant.now().minusSeconds(600)
            }
        setCachedAuthentication(cachedAuthentication)

        every { authenticationService.getAuthentication() } returns ClinicalOneAuthentication("Bearer", "token")

        val authentication = broker.getAuthentication()
        Assertions.assertEquals("Bearer", authentication.tokenType)
        Assertions.assertEquals("token", authentication.accessToken)
        Assertions.assertNull(authentication.expiresAt)
        Assertions.assertNull(authentication.scope)
        Assertions.assertNull(authentication.refreshToken)
    }

    @Test
    fun `loads authentication when cached expires within buffer`() {
        val cachedAuthentication =
            mockk<Authentication> {
                every { expiresAt } returns Instant.now().plusSeconds(25)
            }
        setCachedAuthentication(cachedAuthentication)

        every { authenticationService.getAuthentication() } returns ClinicalOneAuthentication("Bearer", "token")

        val authentication = broker.getAuthentication()
        Assertions.assertEquals("Bearer", authentication.tokenType)
        Assertions.assertEquals("token", authentication.accessToken)
        Assertions.assertNull(authentication.expiresAt)
        Assertions.assertNull(authentication.scope)
        Assertions.assertNull(authentication.refreshToken)
    }

    @Test
    fun `returns cached authentication when still valid`() {
        val cachedAuthentication =
            mockk<Authentication> {
                every { expiresAt } returns Instant.now().plusSeconds(600)
                every { tokenType } returns "Basic"
                every { accessToken } returns "cached_token"
                every { scope } returns null
                every { refreshToken } returns null
            }
        setCachedAuthentication(cachedAuthentication)

        val authentication = broker.getAuthentication()
        Assertions.assertEquals("Basic", authentication.tokenType)
        Assertions.assertEquals("cached_token", authentication.accessToken)
        Assertions.assertNotNull(authentication.expiresAt)
        Assertions.assertNull(authentication.scope)
        Assertions.assertNull(authentication.refreshToken)
    }

    private fun getCachedAuthentication() = cachedAuthorizationProperty.getter.call(broker)

    private fun setCachedAuthentication(authentication: Authentication?) = cachedAuthorizationProperty.setter.call(broker, authentication)
}
