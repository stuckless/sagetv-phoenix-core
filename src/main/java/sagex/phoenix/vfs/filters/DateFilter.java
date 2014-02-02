package sagex.phoenix.vfs.filters;

import java.util.Calendar;
import java.util.Date;

import sagex.phoenix.factory.ConfigurableOption;
import sagex.phoenix.factory.ConfigurableOption.DataType;
import sagex.phoenix.util.DateUtils;
import sagex.phoenix.vfs.IMediaFile;
import sagex.phoenix.vfs.IMediaResource;

public class DateFilter extends Filter {
	private Date myDate = null;

	public DateFilter() {
		super();
		addOption(new ConfigurableOption(OPT_VALUE, "Date", null, DataType.string));
	}

	@Override
	public boolean canAccept(IMediaResource res) {

		if (res instanceof IMediaFile) {
			Calendar myCal = Calendar.getInstance();
			myCal.setTime(myDate);

			Date mediaDate = new Date(((IMediaFile) res).getStartTime());
			Calendar mediaCal = Calendar.getInstance();
			mediaCal.setTime(mediaDate);

			if ((myCal.get(Calendar.MONTH) == mediaCal.get(Calendar.MONTH))
					&& (myCal.get(Calendar.DATE) == mediaCal.get(Calendar.DATE))
					&& (myCal.get(Calendar.YEAR) == mediaCal.get(Calendar.YEAR))) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public void onUpdate() {
		String value = getOption(OPT_VALUE).getString(null);
		if (value == null || value.length() == 0) {
			myDate = new Date(System.currentTimeMillis());
		} else {
			myDate = DateUtils.parseDate(value);
		}
	}
}
