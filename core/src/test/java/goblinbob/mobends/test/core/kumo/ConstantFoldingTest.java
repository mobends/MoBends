package goblinbob.mobends.test.core.kumo;

import com.google.gson.JsonParser;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.VariableTable;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import org.junit.Test;

import static org.junit.Assert.*;

/** What reads nothing per entity is computed once, when the animator loads. */
public class ConstantFoldingTest
{

    private static Expression compile(String json) throws MalformedKumoTemplateException
    {
        return Expression.compile(new JsonParser().parse(json.replace('\'', '"')), ExpressionScope.root(new VariableTable()), Expression.Type.NUMBER);
    }

    private static Expression condition(String json) throws MalformedKumoTemplateException
    {
        return Expression.compile(new JsonParser().parse(json.replace('\'', '"')), ExpressionScope.root(new VariableTable()), Expression.Type.BOOLEAN);
    }

    @Test
    public void languageOperationsOfConstantsAreComputedOnce() throws MalformedKumoTemplateException
    {
        Expression sum = compile("{'mul': [{'add': [1, 2]}, {'sin': [0]}, 4]}");
        assertTrue(sum.isConstant());
        assertEquals(0F, sum.get(null), 0F);
        assertTrue(condition("{'and': [true, {'gt': [3, 2]}]}").isConstant());
        assertEquals(6F, compile("{'if': [{'lt': [1, 2]}, 6, 7]}").get(null), 0F);
    }

    @Test
    public void whatReadsTheEntityOrRemembersIsNot() throws MalformedKumoTemplateException
    {
        assertFalse(compile("{'add': ['speed', 1]}").isConstant());
        assertFalse(condition("{'rose': [true]}").isConstant());
        assertFalse(compile("{'add': [1, 'nodeTicksElapsed']}").isConstant());
    }

}
