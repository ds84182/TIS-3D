package li.cil.tis3d.system.module.execution.compiler;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import li.cil.tis3d.Constants;
import li.cil.tis3d.system.module.execution.MachineState;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitter;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterArithmetic;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterJump;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterJumpEqualsZero;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterJumpGreaterThanZero;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterJumpLessThanZero;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterJumpNotZero;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterJumpRelative;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterMissing;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterMove;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterNeg;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterNop;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterSave;
import li.cil.tis3d.system.module.execution.compiler.instruction.InstructionEmitterSwap;
import li.cil.tis3d.system.module.execution.instruction.Instruction;
import li.cil.tis3d.system.module.execution.instruction.InstructionAdd;
import li.cil.tis3d.system.module.execution.instruction.InstructionAddImmediate;
import li.cil.tis3d.system.module.execution.instruction.InstructionSubtract;
import li.cil.tis3d.system.module.execution.instruction.InstructionSubtractImmediate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compiles TIS-100 assembly code into instructions.
 * <p>
 * Generates exceptions with line and column location if invalid code is encountered.
 */
public final class Compiler {
    /**
     * The maximum number of lines a piece of code may have.
     */
    public static final int MAX_LINES = 20;

    /**
     * The maximum number of characters a single line may have.
     */
    public static final int MAX_COLUMNS = 18;

    // --------------------------------------------------------------------- //

    /**
     * Parse the specified piece of assembly code into the specified machine state.
     * <p>
     * Note that the machine state will be hard reset.
     *
     * @param code  the code to parse and compile.
     * @param state the machine state to store the instructions and debug info in.
     * @throws ParseException if the specified code contains syntax errors.
     */
    public static void compile(final String code, final MachineState state) throws ParseException {
        state.clear();

        final String[] lines = PATTERN_LINES.split(code);
        if (lines.length > MAX_LINES) {
            throw new ParseException(Constants.MESSAGE_TOO_MANY_LINES, MAX_LINES, 0);
        }
        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            lines[lineNumber] = lines[lineNumber].toUpperCase(Locale.ENGLISH);
        }

        state.code = lines;

        try {
            // Parse all lines into the specified machine state.
            final List<Validator> validators = new ArrayList<>();
            for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
                // Enforce max line length.
                if (lines[lineNumber].length() > MAX_COLUMNS) {
                    throw new ParseException(Constants.MESSAGE_LINE_TOO_LONG, lineNumber, MAX_COLUMNS);
                }

                // Get current line, strip comments, trim whitespace and uppercase.
                final Matcher matcher = PATTERN_COMMENT.matcher(lines[lineNumber]);
                final String line = matcher.replaceFirst("").trim();

                // Extract a label, if any, pass the rest onto the instruction parser.
                parseInstruction(parseLabel(line, state), state, lineNumber, validators);
            }

            // Run all registered validators as a post-processing step. This is used
            // to check jumps reference existing labels, for example.
            for (final Validator validator : validators) {
                validator.accept(state);
            }
        } catch (final ParseException e) {
            state.clear();
            state.code = lines;
            throw e;
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Look for a label on the specified line and store it if present.
     *
     * @param line  the line to parse.
     * @param state the machine state to store the label in.
     * @return the remainder of the line, or the full line if there was no label.
     */
    private static String parseLabel(final String line, final MachineState state) {
        final Matcher matcher = PATTERN_LABEL.matcher(line);
        if (matcher.matches()) {
            // Got a label, store it and the address it represents.
            final String label = matcher.group("label");
            state.labels.put(label, state.instructions.size());
            // Return the remainder of the line.
            return matcher.group("rest");
        } else {
            // No label, return line as-is.
            return line;
        }
    }

    /**
     * Look for an instruction on the specified line and store it if present.
     *
     * @param line       the line to parse.
     * @param state      the machine state to store the generated instruction in.
     * @param lineNumber the number of the line we're parsing (for exceptions).
     * @param validators list of validators instruction emitters may add to.
     * @throws ParseException if there was a syntax error.
     */
    private static void parseInstruction(final String line, final MachineState state, final int lineNumber, final List<Validator> validators) throws ParseException {
        // Skip blank lines and empty remainders of label parsing.
        if (Strings.isNullOrEmpty(line)) return;

        final Matcher matcher = PATTERN_INSTRUCTION.matcher(line);
        if (matcher.matches()) {
            // Got an instruction, process arguments and instantiate it.
            final Instruction instruction = EMITTER_MAP.getOrDefault(matcher.group("name"), EMITTER_MISSING).
                    compile(matcher, lineNumber, validators);

            // Remember line numbers for debugging.
            state.lineNumbers.put(state.instructions.size(), lineNumber);

            // Store the instruction in the machine state (after just to skip the -1 :P).
            state.instructions.add(instruction);
        } else {
            // This should be pretty much impossible...
            throw new ParseException(Constants.MESSAGE_UNEXPECTED_TOKEN, lineNumber, 0);
        }
    }

    // --------------------------------------------------------------------- //

    private static final Pattern PATTERN_LINES = Pattern.compile("\r?\n");
    private static final Pattern PATTERN_COMMENT = Pattern.compile("#.*$");
    private static final Pattern PATTERN_LABEL = Pattern.compile("(?<label>[^:]+)\\s*:\\s*(?<rest>.*)");
    private static final Pattern PATTERN_INSTRUCTION = Pattern.compile("^(?<name>[^,\\s]+)\\s*,?\\s*(?<arg1>[^,\\s]+)?\\s*,?\\s*(?<arg2>[^,\\s]+)?\\s*(?<excess>.+)?$");
    private static final InstructionEmitter EMITTER_MISSING = new InstructionEmitterMissing();
    private static final Map<String, InstructionEmitter> EMITTER_MAP;

    static {
        final ImmutableMap.Builder<String, InstructionEmitter> builder = ImmutableMap.<String, InstructionEmitter>builder();

        addInstructionEmitter(builder, new InstructionEmitterArithmetic("ADD", InstructionAdd::new, InstructionAddImmediate::new));
        addInstructionEmitter(builder, new InstructionEmitterJump());
        addInstructionEmitter(builder, new InstructionEmitterJumpEqualsZero());
        addInstructionEmitter(builder, new InstructionEmitterJumpGreaterThanZero());
        addInstructionEmitter(builder, new InstructionEmitterJumpLessThanZero());
        addInstructionEmitter(builder, new InstructionEmitterJumpNotZero());
        addInstructionEmitter(builder, new InstructionEmitterJumpRelative());
        addInstructionEmitter(builder, new InstructionEmitterMove());
        addInstructionEmitter(builder, new InstructionEmitterNeg());
        addInstructionEmitter(builder, new InstructionEmitterNop());
        addInstructionEmitter(builder, new InstructionEmitterSave());
        addInstructionEmitter(builder, new InstructionEmitterArithmetic("SUB", InstructionSubtract::new, InstructionSubtractImmediate::new));
        addInstructionEmitter(builder, new InstructionEmitterSwap());

        EMITTER_MAP = builder.build();
    }

    private static void addInstructionEmitter(final ImmutableMap.Builder<String, InstructionEmitter> builder, final InstructionEmitter emitter) {
        builder.put(emitter.getInstructionName(), emitter);
    }

    private Compiler() {
    }
}
