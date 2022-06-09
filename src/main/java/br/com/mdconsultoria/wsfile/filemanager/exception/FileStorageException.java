package br.com.mdconsultoria.wsfile.filemanager.exception;

public class FileStorageException extends RuntimeException {
	private static final long serialVersionUID = 6919616779289832399L;

	public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }

}
