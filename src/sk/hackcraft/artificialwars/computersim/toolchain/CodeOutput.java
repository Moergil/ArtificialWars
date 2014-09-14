package sk.hackcraft.artificialwars.computersim.toolchain;

import java.io.InputStream;

public interface CodeOutput<O>
{
	O output(InputStream input);
}
