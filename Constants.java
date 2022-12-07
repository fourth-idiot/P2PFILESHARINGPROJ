public class Constants {
    public static final String COMMON_CONFIG_FILE_NAME = "Common.cfg";
    public static final String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    public static final String PEER_INFO_CONFIG_FILE_NAME = "PeerInfo.cfg";
    public static final String WORKING_DIR = System.getProperty("user.dir");
    public static final String ZERO_BITS_HANDSHAKE = "0000000000";

    public static enum MessageType {
		CHOKE(0), UNCHOKE(1), INTERESTED(2), NOT_INTERESTED(3),
        HAVE(4), BITFIELD(5), REQUEST(6), PIECE(7), COMPLETED(8);

		private final int value;

		private MessageType(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}

        public static MessageType getByValue(int value) {
            for (MessageType messageType: MessageType.values()) {
                if (messageType.getValue() == value) {
                    return messageType;
                }
            }
            return null;
        }
    }
}
