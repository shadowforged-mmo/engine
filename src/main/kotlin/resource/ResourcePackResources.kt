package com.shadowforgedmmo.engine.resource

import com.shadowforgedmmo.engine.icon.IconAsset
import com.shadowforgedmmo.engine.model.BlockbenchItemModelAsset
import com.shadowforgedmmo.engine.model.BlockbenchModel
import com.shadowforgedmmo.engine.music.MusicTrackAsset
import com.shadowforgedmmo.engine.sound.SoundAsset

class ResourcePackResources(
    val config: Config,
    val blockbenchModels: Registry<BlockbenchModel>,
    val blockbenchItemModelAssets: Registry<BlockbenchItemModelAsset>,
    val musicTrackAssets: Registry<MusicTrackAsset>,
    val soundAssets: Registry<SoundAsset>,
    val iconAssets: Registry<IconAsset>,
    val iconAssetsWithCooldowns: Set<IconAsset>
)
