import com.haulmont.cli.bintraypublisher.BintrayPublisherPlugin;
import com.haulmont.cuba.cli.CliPlugin;

module com.haulmont.cli.bintraypublisher {
    requires com.haulmont.cuba.cli;
    requires com.google.common;

    requires kodein.di.core.jvm;
    requires kodein.di.generic.jvm;
    requires kotlin.stdlib;
    requires jcommander;

    exports com.haulmont.cli.bintraypublisher;
    opens com.haulmont.cli.bintraypublisher;

    provides CliPlugin with BintrayPublisherPlugin;
}