// MODIFIED FROM https://masterex.github.io/archive/2011/10/23/java-cli-progress-bar.html
/**
 * Ascii progress meter. On completion this will reset itself,
 * so it can be reused
 * <br /><br />
 * 100% ################################################## |
 */
public class ProgressBar {
    private StringBuilder progress;
    private String barMessage;
    private int total; // total is an int representing the total work
    private int done;

    /**
     * initialize progress bar properties.
     */
    public ProgressBar(String barMessage, int total) {
        init();
        this.barMessage = barMessage;
        this.total = total;
    }

    /**
     * called whenever the progress bar needs to be updated.
     * that is whenever progress was made.
     */
    public void next() {
        char[] workchars = {'|', '/', '-', '\\'};
        String format = "\r%s  %3d%% %s %c";

        int percent = (++this.done * 100) / this.total;
        int extrachars = (percent / 2) - this.progress.length();

        while (extrachars-- > 0) {
            progress.append('#');
        }

        System.out.printf(format, this.barMessage, percent, progress,
                workchars[this.done % workchars.length]);

        if (this.done == this.total) {
            System.out.flush();
            System.out.println();
            init();
        }
    }

    // TODO add an end func?

    private void init() {
        this.progress = new StringBuilder(60);
    }
}