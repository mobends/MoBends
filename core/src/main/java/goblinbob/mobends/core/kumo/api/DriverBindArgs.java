package goblinbob.mobends.core.kumo.api;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * What a {@link DriverBinder} gets: its template's fields made into inputs, states and bones,
 * and its state. {@code field} names the template's field, for the errors.
 */
public interface DriverBindArgs
{

    /** The class of the entity animated, or null if it is unknown. */
    @Nullable
    Class<?> entityClass();

    /** An error in the driver's own words ("'mymod:wag' ..."); throw it. */
    MalformedKumoTemplateException error(String message);

    /** The number expression {@code expression}, or the constant {@code otherwise} where the field is left out. */
    NumberInput number(String field, @Nullable ExpressionTemplate expression, float otherwise) throws MalformedKumoTemplateException;

    /** The number expression {@code expression}; a field that has to be there. */
    NumberInput number(String field, @Nullable ExpressionTemplate expression) throws MalformedKumoTemplateException;

    /** The boolean expression {@code expression}, or {@code otherwise} where the field is left out. */
    BooleanInput bool(String field, @Nullable ExpressionTemplate expression, boolean otherwise) throws MalformedKumoTemplateException;

    /**
     * The built-in {@code name} (as {@code "entityWorldX"}), read in double precision: for
     * positions in the world, which a float would round.
     */
    NumberInput entityValue(String field, String name) throws MalformedKumoTemplateException;

    /** The bone {@code name}, as the index the {@link PoseWriter} takes. */
    int bone(String field, @Nullable String name) throws MalformedKumoTemplateException;

    /** The number state the driver steps, named by its {@code inout} field. */
    StateHandle inout(String field, @Nullable String state) throws MalformedKumoTemplateException;

    /**
     * The driver's outputs, as its {@code out} field maps them to number states (null: none).
     * An output not among {@code outputs} is an error.
     */
    Outputs outputs(@Nullable Map<String, String> out, String... outputs) throws MalformedKumoTemplateException;

    /** A number the driver keeps between frames; see {@link BindArgs#slot}. */
    FloatSlot slot(String name, float initial);

    /** Numbers the driver keeps between frames; see {@link BindArgs#slots}. */
    FloatArraySlot slots(String name, int size, float initial);

    /** The states a driver's outputs go to. */
    interface Outputs
    {
        /** The state {@code output} goes to, or null if the file maps it to none. */
        @Nullable
        StateHandle get(String output);
    }

}
