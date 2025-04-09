package player

import android.content.Context
import com.player.pluginlibrary.PlayerFactory

/**
 * <pre>
 *     author : derek
 *     time   : 2025/04/08
 *     desc   :
 *     version:
 * </pre>
 */
class VLCPlayerFactory : PlayerFactory<VlcPlayer>() {

    companion object {
        @JvmStatic
        fun create(): VLCPlayerFactory {
            return VLCPlayerFactory()
        }
    }

    override fun createPlayer(context: Context?): VlcPlayer {
        return VlcPlayer(context)
    }
}