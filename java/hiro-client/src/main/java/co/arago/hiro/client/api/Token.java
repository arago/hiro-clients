/*
 * Copyright (c) 2015 arago GmbH, https://www.arago.co
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package co.arago.hiro.client.api;

/// @addtogroup Authentication Authentication
import java.io.Serializable;

/// @{
/**
 * defines an abstract view of an access token
 *
 * implementations are supposed to hide the complexity of getting an access
 * token
 *
 *
 */
public interface Token extends Serializable {

  /**
   * either returns a valid token or throws an exception
   *
   * the getToken method is supposed to hide any operations involved to actually
   * get a valid access token.
   *
   * Hence calling getToken will automatically perform any necessary operations
   * against the authorization services backing up a specific implementation of
   * Token. This might include things like: - perform a token request against
   * authorization server - automatically refresh a token got earlier if it
   * becomes invalid
   *
   * @return a valid access token as string
   */
  String getToken();

  /**
   * returns string representation of the token
   *
   * @return returns token as string
   *
   */
  @Override
  String toString();
}

///@}
