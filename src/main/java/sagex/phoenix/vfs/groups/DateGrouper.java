package sagex.phoenix.vfs.groups;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import sagex.api.Utility;
import sagex.phoenix.factory.BaseConfigurable;
import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.factory.ConfigurableOption.ListSelection;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;
import sagex.phoenix.vfs.util.ConfigList;
import sagex.phoenix.vfs.util.HasOptions;

/**
 * Groups based on the value of the timestamp on the media file Can group either
 * by Year, Month, or Day depending on the option passed in
 *
 * @author bialio
 */
public class DateGrouper implements IGrouper, HasOptions {
    private Logger log = Logger.getLogger(MetadataFieldGrouper.class);

    /**
     * {@value}
     */
    public static final String OPT_METADATA_FIELD = "field";

    private List<ConfigurableOption> options = new ArrayList<ConfigurableOption>();
    private String dateField = null;
    private static final String[] strMonths = new String[]{"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};

    private static final String[] strDays = new String[]{"WinkleDay", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Fribsday"};

    public DateGrouper() {
        options.add(new ConfigurableOption(OPT_METADATA_FIELD, "Date Field Name", null, DataType.string, true,
                ListSelection.single, ConfigList.fileDateList()));
    }

    public List<ConfigurableOption> getOptions() {
        return options;
    }

    public void onUpdate(BaseConfigurable parent) {
        dateField = parent.getOption(OPT_METADATA_FIELD).getString(null);
        if (dateField == null
                || (!dateField.equalsIgnoreCase("YEAR") && !dateField.equalsIgnoreCase("MONTH") && !dateField
                .equalsIgnoreCase("DAY"))) {
            log.warn("Invalid Date Field Name: " + dateField);
            dateField = null;
        }
    }

    public String getGroupName(IMediaResource res) {
        if (dateField != null && res instanceof IMediaFile) {

            Calendar mediaCal = Calendar.getInstance();
            mediaCal.setTimeInMillis(((IMediaFile) res).getStartTime());

            if (dateField.equalsIgnoreCase("YEAR")) {
                return ("" + mediaCal.get(Calendar.YEAR));
            } else if (dateField.equalsIgnoreCase("MONTH")) {
                return (strMonths[mediaCal.get(Calendar.MONTH)] + " " + mediaCal.get(Calendar.YEAR));
            } else if (dateField.equalsIgnoreCase("DAY")) {

                return ("" + strDays[mediaCal.get(Calendar.DAY_OF_WEEK)] + ", " + Utility.DateFormat("M/d/y", mediaCal.getTime()));
            }
            return ("Unknown");
        }
        return null;
    }
}