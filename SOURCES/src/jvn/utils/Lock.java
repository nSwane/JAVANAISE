/***
 * Lock enumeration
 * Enumeration of the different value of a lock.
 */

package jvn.utils;

import java.io.Serializable;

public enum Lock implements Serializable {
	NL,
	W,
	R,
	WC,
	RC,
	RWC;
}
