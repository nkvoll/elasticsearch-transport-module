/*
 * Copyright (c) 2013, Found AS.
 * See LICENSE for details.
 */

package no.found.elasticsearch.transport.netty;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterNameModule;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.netty.bootstrap.ClientBootstrap;
import org.elasticsearch.common.netty.channel.ChannelPipeline;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.transport.TransportModule;
import org.elasticsearch.transport.netty.FoundNettyTransport;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;

public class TestNettyTransport {
    @Test
    public void testClientBootstrapUpdated() throws Exception {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("transport.type", "no.found.elasticsearch.transport.netty.FoundNettyTransportModule")
                .build();

        ModulesBuilder modules = new ModulesBuilder();

        modules.add(new Version.Module(Version.CURRENT));
        modules.add(new SettingsModule(settings));
        modules.add(new ClusterNameModule(settings));
        modules.add(new TransportModule(settings));

        Injector injector = modules.createInjector();

        FoundNettyTransport transport = injector.getInstance(FoundNettyTransport.class);
        transport.start();

        Field clientBootstrapField = transport.getClass().getSuperclass().getDeclaredField("clientBootstrap");
        clientBootstrapField.setAccessible(true);
        ClientBootstrap clientBootstrap = (ClientBootstrap)clientBootstrapField.get(transport);

        ChannelPipeline pipeline = clientBootstrap.getPipelineFactory().getPipeline();

        FoundSwitchingChannelHandler channelHandler = pipeline.get(FoundSwitchingChannelHandler.class);
        assertNotNull(channelHandler);
    }
}