package ssi.g3.proto;

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: CriptoMessage.proto

public final class CriptoMessage {
  private CriptoMessage() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface GenericMessageOrBuilder extends
      // @@protoc_insertion_point(interface_extends:GenericMessage)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>bytes criptograma = 1;</code>
     */
    com.google.protobuf.ByteString getCriptograma();

    /**
     * <code>bytes iv = 2;</code>
     */
    com.google.protobuf.ByteString getIv();

    /**
     * <code>bytes mac = 3;</code>
     */
    com.google.protobuf.ByteString getMac();
  }
  /**
   * Protobuf type {@code GenericMessage}
   */
  public  static final class GenericMessage extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:GenericMessage)
      GenericMessageOrBuilder {
    // Use GenericMessage.newBuilder() to construct.
    private GenericMessage(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private GenericMessage() {
      criptograma_ = com.google.protobuf.ByteString.EMPTY;
      iv_ = com.google.protobuf.ByteString.EMPTY;
      mac_ = com.google.protobuf.ByteString.EMPTY;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
    }
    private GenericMessage(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      int mutable_bitField0_ = 0;
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!input.skipField(tag)) {
                done = true;
              }
              break;
            }
            case 10: {

              criptograma_ = input.readBytes();
              break;
            }
            case 18: {

              iv_ = input.readBytes();
              break;
            }
            case 26: {

              mac_ = input.readBytes();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return CriptoMessage.internal_static_GenericMessage_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return CriptoMessage.internal_static_GenericMessage_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              CriptoMessage.GenericMessage.class, CriptoMessage.GenericMessage.Builder.class);
    }

    public static final int CRIPTOGRAMA_FIELD_NUMBER = 1;
    private com.google.protobuf.ByteString criptograma_;
    /**
     * <code>bytes criptograma = 1;</code>
     */
    public com.google.protobuf.ByteString getCriptograma() {
      return criptograma_;
    }

    public static final int IV_FIELD_NUMBER = 2;
    private com.google.protobuf.ByteString iv_;
    /**
     * <code>bytes iv = 2;</code>
     */
    public com.google.protobuf.ByteString getIv() {
      return iv_;
    }

    public static final int MAC_FIELD_NUMBER = 3;
    private com.google.protobuf.ByteString mac_;
    /**
     * <code>bytes mac = 3;</code>
     */
    public com.google.protobuf.ByteString getMac() {
      return mac_;
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (!criptograma_.isEmpty()) {
        output.writeBytes(1, criptograma_);
      }
      if (!iv_.isEmpty()) {
        output.writeBytes(2, iv_);
      }
      if (!mac_.isEmpty()) {
        output.writeBytes(3, mac_);
      }
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (!criptograma_.isEmpty()) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(1, criptograma_);
      }
      if (!iv_.isEmpty()) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(2, iv_);
      }
      if (!mac_.isEmpty()) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(3, mac_);
      }
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof CriptoMessage.GenericMessage)) {
        return super.equals(obj);
      }
      CriptoMessage.GenericMessage other = (CriptoMessage.GenericMessage) obj;

      boolean result = true;
      result = result && getCriptograma()
          .equals(other.getCriptograma());
      result = result && getIv()
          .equals(other.getIv());
      result = result && getMac()
          .equals(other.getMac());
      return result;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + CRIPTOGRAMA_FIELD_NUMBER;
      hash = (53 * hash) + getCriptograma().hashCode();
      hash = (37 * hash) + IV_FIELD_NUMBER;
      hash = (53 * hash) + getIv().hashCode();
      hash = (37 * hash) + MAC_FIELD_NUMBER;
      hash = (53 * hash) + getMac().hashCode();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static CriptoMessage.GenericMessage parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static CriptoMessage.GenericMessage parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static CriptoMessage.GenericMessage parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static CriptoMessage.GenericMessage parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static CriptoMessage.GenericMessage parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static CriptoMessage.GenericMessage parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static CriptoMessage.GenericMessage parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static CriptoMessage.GenericMessage parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static CriptoMessage.GenericMessage parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static CriptoMessage.GenericMessage parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static CriptoMessage.GenericMessage parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static CriptoMessage.GenericMessage parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(CriptoMessage.GenericMessage prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code GenericMessage}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:GenericMessage)
        CriptoMessage.GenericMessageOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return CriptoMessage.internal_static_GenericMessage_descriptor;
      }

      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return CriptoMessage.internal_static_GenericMessage_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                CriptoMessage.GenericMessage.class, CriptoMessage.GenericMessage.Builder.class);
      }

      // Construct using CriptoMessage.GenericMessage.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        criptograma_ = com.google.protobuf.ByteString.EMPTY;

        iv_ = com.google.protobuf.ByteString.EMPTY;

        mac_ = com.google.protobuf.ByteString.EMPTY;

        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return CriptoMessage.internal_static_GenericMessage_descriptor;
      }

      public CriptoMessage.GenericMessage getDefaultInstanceForType() {
        return CriptoMessage.GenericMessage.getDefaultInstance();
      }

      public CriptoMessage.GenericMessage build() {
        CriptoMessage.GenericMessage result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public CriptoMessage.GenericMessage buildPartial() {
        CriptoMessage.GenericMessage result = new CriptoMessage.GenericMessage(this);
        result.criptograma_ = criptograma_;
        result.iv_ = iv_;
        result.mac_ = mac_;
        onBuilt();
        return result;
      }

      public Builder clone() {
        return (Builder) super.clone();
      }
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.setField(field, value);
      }
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return (Builder) super.clearField(field);
      }
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return (Builder) super.clearOneof(oneof);
      }
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, Object value) {
        return (Builder) super.setRepeatedField(field, index, value);
      }
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          Object value) {
        return (Builder) super.addRepeatedField(field, value);
      }
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof CriptoMessage.GenericMessage) {
          return mergeFrom((CriptoMessage.GenericMessage)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(CriptoMessage.GenericMessage other) {
        if (other == CriptoMessage.GenericMessage.getDefaultInstance()) return this;
        if (other.getCriptograma() != com.google.protobuf.ByteString.EMPTY) {
          setCriptograma(other.getCriptograma());
        }
        if (other.getIv() != com.google.protobuf.ByteString.EMPTY) {
          setIv(other.getIv());
        }
        if (other.getMac() != com.google.protobuf.ByteString.EMPTY) {
          setMac(other.getMac());
        }
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        CriptoMessage.GenericMessage parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (CriptoMessage.GenericMessage) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private com.google.protobuf.ByteString criptograma_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>bytes criptograma = 1;</code>
       */
      public com.google.protobuf.ByteString getCriptograma() {
        return criptograma_;
      }
      /**
       * <code>bytes criptograma = 1;</code>
       */
      public Builder setCriptograma(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        criptograma_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bytes criptograma = 1;</code>
       */
      public Builder clearCriptograma() {
        
        criptograma_ = getDefaultInstance().getCriptograma();
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString iv_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>bytes iv = 2;</code>
       */
      public com.google.protobuf.ByteString getIv() {
        return iv_;
      }
      /**
       * <code>bytes iv = 2;</code>
       */
      public Builder setIv(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        iv_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bytes iv = 2;</code>
       */
      public Builder clearIv() {
        
        iv_ = getDefaultInstance().getIv();
        onChanged();
        return this;
      }

      private com.google.protobuf.ByteString mac_ = com.google.protobuf.ByteString.EMPTY;
      /**
       * <code>bytes mac = 3;</code>
       */
      public com.google.protobuf.ByteString getMac() {
        return mac_;
      }
      /**
       * <code>bytes mac = 3;</code>
       */
      public Builder setMac(com.google.protobuf.ByteString value) {
        if (value == null) {
    throw new NullPointerException();
  }
  
        mac_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bytes mac = 3;</code>
       */
      public Builder clearMac() {
        
        mac_ = getDefaultInstance().getMac();
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }


      // @@protoc_insertion_point(builder_scope:GenericMessage)
    }

    // @@protoc_insertion_point(class_scope:GenericMessage)
    private static final CriptoMessage.GenericMessage DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new CriptoMessage.GenericMessage();
    }

    public static CriptoMessage.GenericMessage getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<GenericMessage>
        PARSER = new com.google.protobuf.AbstractParser<GenericMessage>() {
      public GenericMessage parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
          return new GenericMessage(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<GenericMessage> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<GenericMessage> getParserForType() {
      return PARSER;
    }

    public CriptoMessage.GenericMessage getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_GenericMessage_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_GenericMessage_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023CriptoMessage.proto\">\n\016GenericMessage\022" +
      "\023\n\013criptograma\030\001 \001(\014\022\n\n\002iv\030\002 \001(\014\022\013\n\003mac\030" +
      "\003 \001(\014B\017B\rCriptoMessageb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_GenericMessage_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_GenericMessage_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_GenericMessage_descriptor,
        new java.lang.String[] { "Criptograma", "Iv", "Mac", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
