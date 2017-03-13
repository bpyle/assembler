 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assembler;

/**
 *
 * @author bpyle
 */
public class InvalidLineFormat extends RuntimeException {

    InvalidLineFormat(String string) {
        super(string);
    }
    
}
