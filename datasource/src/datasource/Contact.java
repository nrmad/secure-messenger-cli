package datasource;

import java.util.Objects;

public class Contact {

private String cid;
private String alias;

public Contact(String cid, String alias){
    this.cid = cid;
    this.alias = alias;
}

    public String getCid() {
        return cid;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(cid, contact.cid) &&
                Objects.equals(alias, contact.alias);
    }
}
