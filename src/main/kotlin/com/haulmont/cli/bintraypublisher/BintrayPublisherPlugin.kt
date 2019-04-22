package com.haulmont.cli.bintraypublisher

import com.google.common.eventbus.Subscribe
import com.haulmont.cuba.cli.CliPlugin
import com.haulmont.cuba.cli.HasResources
import com.haulmont.cuba.cli.ResourcesPath
import com.haulmont.cuba.cli.event.InitPluginEvent

class BintrayPublisherPlugin : CliPlugin {
    override val apiVersion = 5

    override val resources: ResourcesPath = HasResources("/com/haulmont/cli/bintraypublisher/")

    @Subscribe
    fun onInit(event: InitPluginEvent) {
        event.commandsRegistry {
            command("bintray", BintraySetupCommand())
        }
    }
}