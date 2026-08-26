package io.github.alexeyaleksandrov.jacademicsupport.dto.dst;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Serialises a DTO strictly by its Java field names.
 *
 * Needed because Lombok getters for fields like {@code mT}, {@code nFamilies} or
 * {@code nClusters} produce {@code getMT()} / {@code getNFamilies()}, from which
 * Jackson derives the mangled names {@code mt} / {@code nfamilies}. Combined with a
 * {@code @JsonProperty} on the field that yields two JSON keys differing only in
 * case, which strict parsers (PowerShell's ConvertFrom-Json, Python's json with
 * duplicate detection) reject. Taking the field names and ignoring getters keeps
 * exactly one key per property.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@JacksonAnnotationsInside
@JsonAutoDetect(
        fieldVisibility    = JsonAutoDetect.Visibility.ANY,
        getterVisibility   = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public @interface DstJsonFields {
}
