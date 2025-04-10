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
        "x86,x86",
        "i386,x86",
        "i486,x86",
        "i586,x86",
        "i686,x86",
        "amd64,amd64",
        "x86_64,amd64",
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
