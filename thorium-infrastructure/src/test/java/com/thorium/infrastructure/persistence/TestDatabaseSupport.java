package com.thorium.infrastructure.persistence;

import com.thorium.infrastructure.ApplicationBootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestDatabaseSupport {

    private TestDatabaseSupport() {
    }

    public static ApplicationBootstrap createBootstrap() throws IOException {
        Path dbFile = Files.createTempFile("thorium-test-", ".db");
        dbFile.toFile().deleteOnExit();
        return ApplicationBootstrap.create(dbFile);
    }
}
