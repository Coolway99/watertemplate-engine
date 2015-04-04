package org.watertemplate.interpreter.parser.abs;

import org.watertemplate.TemplateMap;
import org.watertemplate.exception.TemplateException;

import static org.watertemplate.TemplateMap.Arguments;
import static org.watertemplate.TemplateMap.TemplateCollection;

class ForCommand implements AbstractSyntaxTree.Command {

    private final String variableName;
    private final IdCommand collectionIdCommand;
    private final AbstractSyntaxTree.Command forStatements;
    private final AbstractSyntaxTree.Command elseStatements;

    public ForCommand(final String variableName, final IdCommand collectionIdCommand, final AbstractSyntaxTree.Command forStatements) {
        this.variableName = variableName;
        this.collectionIdCommand = collectionIdCommand;
        this.forStatements = forStatements;
        this.elseStatements = (arguments) -> "";
    }

    public ForCommand(final String variableName, final IdCommand collectionIdCommand, final AbstractSyntaxTree.Command forStatements, final AbstractSyntaxTree.Command elseStatements) {
        this.variableName = variableName;
        this.collectionIdCommand = collectionIdCommand;
        this.forStatements = forStatements;
        this.elseStatements = elseStatements;
    }

    @Override
    public Object run(final Arguments arguments) {
        Object collection = collectionIdCommand.run(arguments);

        if (!(collection instanceof TemplateCollection)) {
            throw new TemplateException("Cannot iterate if collection was not added by addCollection method.");
        }

        TemplateCollection templateCollection = (TemplateCollection) collection;

        if (templateCollection.iterator() == null || !templateCollection.iterator().hasNext()) {
            return elseStatements.run(arguments);
        }

        StringBuilder sb = new StringBuilder();

        for (final Object item : templateCollection) {
            arguments.addMappedObject(variableName, item, templateCollection.getMapper());
            sb.append(forStatements.run(arguments));
        }

        arguments.remove(variableName);
        return sb.toString();
    }
}
