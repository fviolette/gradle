/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.nativeplatform.internal;

import org.gradle.api.internal.file.collections.DirectoryFileTreeFactory;
import org.gradle.api.internal.hash.FileHasher;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.language.nativeplatform.internal.incremental.CompilationStateCacheFactory;
import org.gradle.language.nativeplatform.internal.incremental.IncrementalNativeCompiler;
import org.gradle.nativeplatform.internal.BinaryToolSpec;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolBackedCompiler;
import org.gradle.nativeplatform.toolchain.internal.CommandLineToolInvocationWorker;
import org.gradle.nativeplatform.toolchain.internal.NativeCompileSpec;
import org.gradle.nativeplatform.toolchain.internal.NativeCompilerFactory;
import org.gradle.nativeplatform.toolchain.internal.OutputCleaningCompiler;
import org.gradle.nativeplatform.toolchain.internal.ParallelCompiler;

public class DefaultNativeCompilerFactory implements NativeCompilerFactory {
    private final BuildOperationExecutor buildOperationExecutor;
    private final FileHasher hasher;
    private final CompilationStateCacheFactory compilationStateCacheFactory;
    private final DirectoryFileTreeFactory directoryFileTreeFactory;

    public DefaultNativeCompilerFactory(BuildOperationExecutor buildOperationExecutor, FileHasher hasher, CompilationStateCacheFactory compilationStateCacheFactory, DirectoryFileTreeFactory directoryFileTreeFactory) {
        this.buildOperationExecutor = buildOperationExecutor;
        this.hasher = hasher;
        this.compilationStateCacheFactory = compilationStateCacheFactory;
        this.directoryFileTreeFactory = directoryFileTreeFactory;
    }

    @Override
    public <T extends BinaryToolSpec> Compiler<T> compiler(CommandLineToolBackedCompiler<T> compiler, CommandLineToolInvocationWorker commandLineToolInvocationWorker) {
        return new ParallelCompiler<T>(buildOperationExecutor, commandLineToolInvocationWorker, compiler);
    }

    @Override
    public <T extends NativeCompileSpec> Compiler<T> incrementalAndParallelCompiler(CommandLineToolBackedCompiler<T> compiler, CommandLineToolInvocationWorker commandLineToolInvocationWorker, CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory, CPreprocessorDialect dialect, String outputFileSuffix) {
        return new IncrementalNativeCompiler<T>(
            hasher,
            compilationStateCacheFactory,
            new OutputCleaningCompiler<T>(
                new ParallelCompiler<T>(
                    buildOperationExecutor,
                    commandLineToolInvocationWorker,
                    compiler),
                compilerOutputFileNamingSchemeFactory,
                outputFileSuffix),
            dialect,
            directoryFileTreeFactory);
    }
}