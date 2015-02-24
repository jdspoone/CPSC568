package casa.util.unused;

import casa.util.PropertiesMap;
import casa.util.Property;
import casa.util.PropertyException;

import java.math.BigDecimal;

/**
 * A <code>PropertyNode</code> object is a node in a
 * <code>LogicalPropositionTree</code> that represents a <var>variable</var>.
 * It can possibly be evaluated to a boolean value, a number or a string if
 * given the context of a <code>PropertiesMap</code>.
 *
 * A <var>variable</var> is a stored property in a <code>PropertiesMap</code>
 * that evaluates to a <var>boolean</var>, a <var>string</var> or a
 * <var>number</var>.<br>
 * A <var>boolean</var> the value <code>true</code> or the value
 * <code>false</code>.<br>
 * A <var>string</var> is a character string.<br>
 * A <var>number</var> is either a integer or floating point number.
 *
 * <p>Copyright: Copyright 2003-2014, Knowledge Science Group, University of Calgary.
 * Permission to use, copy, modify, distribute and sell this software and its
 * documentation for any purpose is hereby granted without fee, provided that
 * the above copyright notice appear in all copies and that both that copyright
 * notice and this permission notice appear in supporting documentation.  The
 * Knowledge Science Group makes no representations about the suitability of
 * this software for any purpose.  It is provided "as is" without express or
 * implied warranty.</p>
 *
 * @author Jason Heard
 * @version 0.9
 * @deprecated (rck) The classes BooleanNode, DataNode, LogicalNode, LogicalPropoisitonTree,
 * LogicalPropositionTreeException, LogicalPropositionTreeNode, NumberLiteralNode,
 * PropositionNode, and StringLiteralNode taken together appear to be unused in CASA.
 */

@Deprecated
public class PropertyNode implements BooleanNode, DataNode {
  /**
   * The name of the property that this node represents.
   */
  private String propertyName = null;

  /**
   * Creates a new <code>PropertyNode</code> without setting the name of the
   * property that this node represents.
   */
  public PropertyNode () {
  }

  /**
   * Creates a new <code>PropertyNode</code> that represents the property with
   * the given name.
   *
   * @param newPropertyName The name of the property that new new node
   * represents.
   */
  public PropertyNode (String newPropertyName) {
    this.propertyName = newPropertyName;
  }

  /**
   * Retrieves the name of the property that this node represents.
   *
   * @return The name of the property that this node represents.
   */
  public String getPropertyName () {
    return propertyName;
  }

  /**
   * Sets the name of the property that this node represents.
   *
   * @param newPropertyName The name of the property that this node represents.
   */
  public void setPropertyName (String newPropertyName) {
    this.propertyName = newPropertyName;
  }

  /**
   * Returns the truth value of this node with the given context, either
   * <code>true</code> or <code>false</code>.
   *
   * @param map The context within which we are evaluating the truth value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return The truth value of this node with the given context, either
   * <code>true</code> or <code>false</code>.
   * @throws LogicalPropositionTreeException If there was a problem determining
   * the truth value.  Usually this happens because a property was referenced
   * as the wrong type or did not exist.
   */
  public boolean evaluate (PropertiesMap map) throws LogicalPropositionTreeException {
    if (propertyName == null) {
      throw new LogicalPropositionTreeException ("Property name is not set!");
    }

    try {
      return map.getBoolean (propertyName);
    } catch (PropertyException e) {
      if (map.hasProperty (propertyName)) {
        throw new LogicalPropositionTreeException ("Property '" + propertyName + "' isn't boolean!", e);
      } else {
        throw new LogicalPropositionTreeException ("Property '" + propertyName + "' does not exist!", e);
      }
    }
  }

  /**
   * Determines whether this node can evaluate to a number with the given
   * context.  Returns <code>true</code> if a call to <code>getNumber()</code>
   * with the same context won't throw an exception; <code>false</code>
   * otherwise.
   *
   * @param map The context within which we are evaluating the truth value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return <code>true</code> if a call to <code>getNumber()</code> with the
   * same context won't throw an exception; <code>false</code> otherwise.
   */
  public boolean isANumber (PropertiesMap map) {
    if (propertyName == null) {
      return false;
    }

    int propertyType;
    try {
      propertyType = map.getType (propertyName);
    } catch (NullPointerException e) {
      return false;
    }

    if (propertyType == Property.INTEGER || propertyType == Property.LONG ||
        propertyType == Property.FLOAT || propertyType == Property.DOUBLE) {
      return true;
    }

    return false;
  }

  /**
   * Determines whether this node can evaluate to a string with the given
   * context.  Returns <code>true</code> if a call to <code>getString()</code>
   * with the same context won't throw an exception; <code>false</code>
   * otherwise.
   *
   * @param map The context within which we are evaluating the truth value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return <code>true</code> if a call to <code>getString()</code> with the
   * same context won't throw an exception; <code>false</code> otherwise.
   */
  public boolean isAString (PropertiesMap map) {
    if (propertyName == null) {
      return false;
    }

    int propertyType;
    try {
      propertyType = map.getType (propertyName);
    } catch (NullPointerException e) {
      return false;
    }

    if (propertyType == Property.STRING) {
      return true;
    }

    return false;
  }

  /**
   * Retrieves this node's numerical value.  Returns a <code>BigDecimal</code>
   * containing the numerical value of this node with the given context.
   *
   * @param map The context within which we are evaluating the numerical value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return A <code>BigDecimal</code> containing the numerical value of this
   * node with the given context.
   * @throws LogicalPropositionTreeException If there was a problem determining
   * whether this node represents a number.  Usually this happens because a
   * property was referenced as the wrong type or did not exist.
   */
  public BigDecimal getNumber (PropertiesMap map) throws LogicalPropositionTreeException {
    if (propertyName == null) {
      throw new LogicalPropositionTreeException ("Property name is not set!");
    }

    int propertyType;
    try {
      propertyType = map.getType (propertyName);
    } catch (NullPointerException e) {
      throw new LogicalPropositionTreeException ("Property '" + propertyName + "' does not exist!");
    }

    try {
      if (propertyType == Property.INTEGER) {
        return new BigDecimal (map.getInteger (propertyName));
      } else if (propertyType == Property.LONG) {
        return new BigDecimal (map.getLong (propertyName));
      } else if (propertyType == Property.FLOAT) {
        return new BigDecimal (map.getFloat (propertyName));
      } else if (propertyType == Property.DOUBLE) {
        return new BigDecimal (map.getDouble (propertyName));
      }

      if (map.hasProperty (propertyName)) {
        throw new LogicalPropositionTreeException ("Property '" + propertyName + "' isn't a number!");
      } else {
        throw new LogicalPropositionTreeException ("Property '" + propertyName + "' does not exist!");
      }
    } catch (PropertyException e) {
      // Should never happen.
      throw new Error ("Impossible block reached!");
    }
  }

  /**
   * Retrieves this node's string value.  Returns a <code>String</code>
   * that is the string value of this node with the given context.
   *
   * @param map The context within which we are evaluating the string value.
   * Defines the type and value of all properties in the tree during the
   * evaluation.
   * @return A <code>String</code> that is the string value of this node with
   * the given context.
   * @throws LogicalPropositionTreeException If there was a problem determining
   * whether this node represents a number.  Usually this happens because a
   * property was referenced as the wrong type or did not exist.
   */
  public String getString (PropertiesMap map) throws LogicalPropositionTreeException {
    if (propertyName == null) {
      throw new LogicalPropositionTreeException ("Property name is not set!");
    }

    try {
      return map.getString (propertyName);
    } catch (PropertyException e) {
      if (map.hasProperty (propertyName)) {
        throw new LogicalPropositionTreeException ("Property '" + propertyName + "' isn't a string!", e);
      } else {
        throw new LogicalPropositionTreeException ("Property '" + propertyName + "' does not exist!", e);
      }
    }
  }
}