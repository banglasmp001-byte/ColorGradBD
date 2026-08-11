package com.ahmednotxgamer.colorgradebd.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Root configuration object for ColorGrade BD.
 * Serialized to/from JSON on disk.
 */
public class ColorGradeConfig {

    /** Config file format version for forward-compatibility. */
    public int configVersion = 1;

    /** Global post-processing settings. */
    public GlobalColorSettings global = new GlobalColorSettings();

    /** Per-element grading profiles. */
    public List<ElementColorSettings> elements = new ArrayList<>();

    public ColorGradeConfig() {}
}
