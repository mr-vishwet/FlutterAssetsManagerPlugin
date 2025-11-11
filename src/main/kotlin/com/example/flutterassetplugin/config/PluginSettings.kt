package com.example.flutterassetplugin.config
// Update File: src/main/kotlin/com/example/flutterassetplugin/config/PluginSettings.kt

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "FlutterAssetsManagerSettings",
    storages = [Storage("flutter_assets_manager.xml")]
)
class PluginSettings : PersistentStateComponent<PluginSettings.State> {

    data class State(
        var assetsFolderPath: String = "",
        var namingConvention: String = "camelCase"
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var assetsFolderPath: String
        get() = myState.assetsFolderPath
        set(value) {
            myState.assetsFolderPath = value
        }

    var namingConvention: String
        get() = myState.namingConvention
        set(value) {
            myState.namingConvention = value
        }

    companion object {
        fun getInstance(project: Project): PluginSettings =
            project.getService(PluginSettings::class.java)
    }
}