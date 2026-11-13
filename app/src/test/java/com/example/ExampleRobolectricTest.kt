package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.network.BedrockProtocolService
import com.example.network.XboxLiveAuthManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name and verify xbox ecdsa chain generation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BugrokTool", appName)

        val authManager = XboxLiveAuthManager()
        val session = authManager.buildSignedBedrockChainSession(
            gamertag = "KraaBedrock",
            customXstsToken = null
        )
        assertEquals("KraaBedrock", session.gamertag)
        assertTrue(session.ecdsaPublicKeyBase64.isNotBlank())
        assertTrue(session.xuid.startsWith("25354"))

        val protocolService = BedrockProtocolService(context)
        val cleanMotd = protocolService.stripMinecraftFormatting("§b§lThe Hive §r§7- §aBedrock")
        assertEquals("The Hive - Bedrock", cleanMotd)
    }
}
