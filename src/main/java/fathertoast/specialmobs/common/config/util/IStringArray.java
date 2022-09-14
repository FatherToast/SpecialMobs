package fathertoast.specialmobs.common.config.util;

import java.util.List;

/**
 * An object that can be serialized to toml as a string array.
 */
public interface IStringArray {
    /** @return A list of strings that will represent this object when written to a toml file. */
    List<String> toStringList();
}