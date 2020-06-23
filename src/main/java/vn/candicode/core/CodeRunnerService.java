package vn.candicode.core;

import java.io.File;

/**
 * Potential Improvements:
 * <ul>
 *     <li>Find a way to get more effectively elapsed time by process</li>
 *     <li>Find a way to get effective consumed memory</li>
 * </ul>
 */
public interface CodeRunnerService {
    CompileResult compile(File root, String language);

    ExecutionResult run(File root, long clock, String language);

    void cleanGarbageFiles(File root, String lang);
}
