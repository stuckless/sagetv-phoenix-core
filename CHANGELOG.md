# 3.1.0
* Fixed Fanart color issues
* Fanart Scaled automatically to max screen size on download
* On Save, will try to fetch fanart, if fanart is missing
* Added Config option to rescale source fanart
* Refactored "timer" code to use common Executors


# 3.0.4
* Fixed possible plugin deadlock when using SageTVEventBus

# 3.0.3
* Added support for font icon mappings

# 3.0.2
* Xml Validation will not completely fail if there are missing views, filters, sources, etc.  It will continue to load the views and flag the views with errors.
* Fixed bug where WeatherAPI was generating with compile errors.