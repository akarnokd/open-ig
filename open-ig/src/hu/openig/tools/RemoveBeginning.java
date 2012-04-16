package hu.openig.tools;

import java.io.RandomAccessFile;

public class RemoveBeginning {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		RandomAccessFile raf = new RandomAccessFile("../open-ig/audio/en/messages/new_caroline_virus.wav", "rw");
		int len = (2205) * 1;
		if (len % 2 == 1) {
			len++;
		}
		raf.seek(4);
		int clen = Integer.reverseBytes(raf.readInt());
		raf.seek(4);
		raf.writeInt(Integer.reverseBytes(clen - len));
		raf.seek(0x28);
		int dlen = Integer.reverseBytes(raf.readInt());
		raf.seek(0x28);
		raf.writeInt(Integer.reverseBytes(dlen - len));
		
		byte[] buffer = new byte[len];
		long rp = 0x2C + buffer.length;
		long wp = 0x2C;
		int c = 0;
		do {
			raf.seek(rp);
			c = raf.read(buffer);
			raf.seek(wp);
			raf.write(buffer, 0, c);
			rp += c;
			wp += c;
		} while (c == buffer.length);
		raf.setLength(raf.getFilePointer());
		raf.close();
		
	}

}
