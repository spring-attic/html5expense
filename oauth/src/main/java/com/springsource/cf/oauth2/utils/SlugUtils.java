package com.springsource.cf.oauth2.utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utilities for generating slugs: a short, meaningful name for a resource that can be used in the resource's friendly URL path.
 * @author Keith Donald
 */
public class SlugUtils {
	
	  private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
	  
	  private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

	  /**
	   * Convert the String input to a slug.
	   */
	  public static String toSlug(String input) {
		if (input == null) {
			throw new IllegalArgumentException("Input cannot be null");
		}
	    String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
	    String normalized = Normalizer.normalize(nowhitespace, Form.NFD);
	    String slug = NONLATIN.matcher(normalized).replaceAll("");
	    return slug.toLowerCase(Locale.ENGLISH);
	  }

}