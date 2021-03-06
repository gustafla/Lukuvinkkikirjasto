/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package lukuvinkkikirjasto.ui;

import org.junit.*;

import lukuvinkkikirjasto.domain.ReadingTipService;

import static org.mockito.Mockito.*;

public class UnknownTest {

    IO io;
    Unknown unknown;
    ReadingTipService rtService;

    @Before
    public void setUp() {
        io = mock(IO.class);
        rtService = mock(ReadingTipService.class);
        unknown = new Unknown(io, rtService);
    }

    @Test
    public void printsRightMessage() {
        unknown.execute();
        verify(io).output("Command not found"); 
    }
}
