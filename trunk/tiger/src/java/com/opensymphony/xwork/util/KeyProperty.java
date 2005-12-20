package com.opensymphony.xwork.util;

/**
 * <!-- START SNIPPET: description -->
 * <p/>Sets the KeyProperty for type conversion.
 * <!-- END SNIPPET: description -->
 *
 * <p/> <u>Annotation usage:</u>
 *
 * <!-- START SNIPPET: usage -->
 * <p/>The KeyProperty annotation must be applied at method level.
 * <!-- END SNIPPET: usage -->
 * This annotation should be used with Generic types, if the key property of the key element needs to be specified.
 * This defaults to id.
 *
 * <p/> <u>Annotation parameters:</u>
 *
 * <!-- START SNIPPET: parameters -->
 * <table>
 * <thead>
 * <tr>
 * <th>Parameter</th>
 * <th>Required</th>
 * <th>Default</th>
 * <th>Description</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr>
 * <td>id</td>
 * <td>no</td>
 * <td>id</td>
 * <td>The key property value.</td>
 * </tr>
 * </tbody>
 * </table>
 * <!-- END SNIPPET: parameters -->
 *
 * <p/> <u>Example code:</u>
 * The key property for users in this example is the <code>userName</code> attribute.
 *
 * <pre>
 * <!-- START SNIPPET: example -->
 * List<User> users = null;
 *
 * @KeyProperty( value = "userName" )
 * public void setUsers(List<User> users) {
 *   this.users = users;
 * }
 * <!-- END SNIPPET: example -->
 * </pre>
 *
 * @author Patrick Lightbody
 * @author Rainer Hermanns
 */
public @interface KeyProperty {
    String value() default "id";
}
