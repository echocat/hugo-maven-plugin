package org.echocat.maven.plugins.hugo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.util.Optional;

import org.echocat.maven.plugins.hugo.model.Architecture.ArchNameProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

public class ArchitectureTest {

    @ParameterizedTest
    @CsvSource(value = {
        "x86,x32",
        "i386,x32",
        "i486,x32",
        "i586,x32",
        "i686,x32",
        "amd64,x64",
        "x86_64,x64",
        "aarch64,arm64",
        "arm64,arm64"
    })
    void findArchitecture(String arch, Architecture expected) {
        try (MockedStatic<ArchNameProvider> mocked = mockStatic(ArchNameProvider.class)) {
            mocked.when(ArchNameProvider::get).thenReturn(arch);
            final Optional<Architecture> actual = Architecture.findArchitecture();
            assertTrue(actual.isPresent());
            assertEquals(expected, actual.get());
        }
    }

}
