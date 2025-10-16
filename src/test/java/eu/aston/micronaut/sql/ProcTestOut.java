package eu.aston.micronaut.sql;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class ProcTestOut {
    private String subject;
    private String scramble;
    private String firstChar;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getScramble() {
        return scramble;
    }

    public void setScramble(String scramble) {
        this.scramble = scramble;
    }

    public String getFirstChar() {
        return firstChar;
    }

    public void setFirstChar(String firstChar) {
        this.firstChar = firstChar;
    }
}
